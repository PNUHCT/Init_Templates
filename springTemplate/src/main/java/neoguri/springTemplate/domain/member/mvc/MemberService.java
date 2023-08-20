package neoguri.springTemplate.domain.member.mvc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import neoguri.springTemplate.domain.member.entity.Member;
import neoguri.springTemplate.exception.dto.BusinessLogicException;
import neoguri.springTemplate.exception.exceptionCode.ExceptionCode;
import neoguri.springTemplate.security.filter.JwtVerificationFilter;
import neoguri.springTemplate.security.util.CustomAuthorityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import neoguri.springTemplate.oauth2.kakao.KakaoProfileVo;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final RedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final CustomAuthorityUtils customAuthorityUtils;
    private final JwtVerificationFilter jwtVerificationFilter;

    @Getter
    @Value("${oauth.kakao.initialKey}")
    private String initialKey;

    @Transactional
    public Member createMember(Member member) {
        verifyNotExistEmail(member.getEmail());
        member.defaultProfile();

        return memberRepository.save(member);
    }


    /**
     * 들어오는 값에 따라, 다음의 Patch 요청을 개별적으로 관리 가능합니다. (null이 아닐경우 수정되는 방식 미적용)
     * 1. 회원 정보 변경 가능
     * 2. 회원 상태 변경 가능
     * 비즈니스 로직은 Member Entity 내에서 처리
     */
    @Transactional
    public void modifyMember(Member member, Long memberId) {
        Member existMember = new Member().verifyMember(memberRepository.findById(memberId));

        String encodedPassword = null;
        Optional.ofNullable(encodedPassword).ifPresent(existMember::modifyPassword);
        Optional.ofNullable(member.getNickname()).ifPresent(existMember::modifyNickname);
        Optional.ofNullable(member.getProfile()).ifPresent(existMember::modifyProfile);  // 현재 profile의 경우 단순 URI상태. 추후 파일로 변경 예정
        Optional.ofNullable(member.getMemberStatus()).ifPresent(existMember::modifyMemberStatus);

        memberRepository.save(existMember);
    }

    /**
     * Member 완전 삭제시 사용
     */
    @Transactional
    public void removeMember(Long memberId) {
        Member verifyMember = new Member().verifyMember(memberRepository.findById(memberId));
        memberRepository.delete(verifyMember);
    }


    /**
     * Member Entity클래스 내 구현한 회원탈퇴 메소드(withdrawMember 호출)
     */
    @Transactional
    public void withdrawMember(Long memberId) {
        Member verifyMember = new Member().verifyMember(memberRepository.findById(memberId));
        verifyMember.withdrawMember();
        memberRepository.save(verifyMember);
    }

    /**
     * 현재는 Controller에서 String 반환중
     *
     * @param memberId : 추후 시큐리티 구현 후 적용될 예정
     * @return : 1명의 member 정보를 반환 => 맞춰서 ResponseDto 제작 예정
     */
    public Member findMember(Long memberId) {
        Member member = new Member().verifyMember(memberRepository.findById(memberId));
        return member;
    }


    /**
     * 전체 회원 조회용 (Default는 10계정)
     *
     * @param pageable : page, size, sort 등 사용 가능
     * @return Page 구조
     */
    public Page<Member> findMembers(Pageable pageable) {
        return memberRepository.findAll(pageable);
    }


    /**
     * 이메일 중복 확인 메소드. 이메일 존재시 예외 발생
     */
    public void verifyNotExistEmail(String email) {
        Optional<Member> optionalEmail = memberRepository.findByEmail(email);
        if (optionalEmail.isPresent()) throw new BusinessLogicException(ExceptionCode.EMAIL_EXIST);
    }


    /**
     * for Kakao
     */

    /**
     * 카카오 외부 로그인 전용 멤버 생성 및 검증 메소드
     * @param kakaoProfile, kakaoAccessToken
     * @return member
     */
    @Transactional
    public Member createKakaoMember (KakaoProfileVo kakaoProfile, String kakaoAccessToken) {
        // 중복 가입 방지 로직 추가
        Optional<Member> optMember;
        if(kakaoProfile.getKakao_account().getEmail()==null) optMember = memberRepository.findByEmail(kakaoProfile.getId().toString()+"@uyouboodan.com");
        else optMember = memberRepository.findByEmail(kakaoProfile.getKakao_account().getEmail());

        if(optMember.isEmpty()) {
            Member member = Member.builder()
                    .memberId(kakaoProfile.getId())
                    .nickname("Mock"+ kakaoProfile.getId())
                    .password(passwordEncoder.encode(getInitialKey())) // yml을 통해 시스템 변수 default값 설정해둠
                    .oauthId(String.valueOf(kakaoProfile.getId()))
                    .oauthAccessToken(kakaoAccessToken)
                    .memberStatus(Member.MemberStatus.MEMBER_ACTIVE)
                    .build();
            if (kakaoProfile.getKakao_account().getEmail()==null) member.modifyEmail(kakaoProfile.getId().toString()+"@uyouboodan.com"); // email 수집 미동의시, 자사 email로 가입됨
            else member.modifyEmail(kakaoProfile.getKakao_account().getEmail());

            member.defaultProfile();
            List<String> roles = customAuthorityUtils.createRoles(member.getEmail());
            member.setRoles(roles);

            jwtVerificationFilter.setOauthSecurityContext(member);

            return memberRepository.save(member);
        }
        else {
            // 기존 회원으로 가입되어 있을 경우, 저장된 AccessToken 을 최신화 해줌 (로그아웃을 위함)
            Member member = optMember.get();
            member.modifyOauthToken(kakaoAccessToken);
            return memberRepository.save(member);
        }
    }

    /**
     * Redis에 저장된 RefreshToken을 제거 : RefreshToken의 key값이 RTKey + memberId로 되어 있기 때문에 존재하면 지우는 방식
     * Redis에 들어온 Authorization으로부터 parsing한 AccessToken을 저장
     * AccessToken을 key, Logout을 value, Expiration은 기존 만료시간 - 현재시간으로 저장하여 시간이 지나면 Redis에서 자동 delete 되게 구현
     * @param memberId
     * @param accessToken
     * @param expiration
     */
    public void verifyMemberFromRedis(Long memberId, String accessToken, Long expiration) {
        // refreshToken 잘 있나 확인 (곧 지워질 refreshToken)
        System.out.println(redisTemplate.opsForValue().get("RTKey"+memberId).toString());

        if(redisTemplate.opsForValue().get("RTKey"+memberId)!=null) redisTemplate.delete("RTKey" + memberId);
        redisTemplate.opsForValue().set(accessToken, "Logout", expiration, TimeUnit.MILLISECONDS);

        // 로그아웃 후 AccessToken이 Redis에 잘 저장됬는지 확인
        System.out.println(redisTemplate.opsForValue().get(accessToken).toString());
    }

}
