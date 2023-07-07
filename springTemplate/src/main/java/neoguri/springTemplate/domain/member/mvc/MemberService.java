package neoguri.springTemplate.domain.member.mvc;

import lombok.RequiredArgsConstructor;
import neoguri.springTemplate.domain.member.entity.Member;
import neoguri.springTemplate.exception.dto.BusinessLogicException;
import neoguri.springTemplate.exception.exceptionCode.ExceptionCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

//    private final PasswordEncoder passwordEncoder; // Spring Security에서 제공하는 모듈
    private final MemberRepository memberRepository;

    @Transactional
    public Member createMember(Member member) {
        verifyNotExistEmail(member.getEmail());
        member.defaultProfile();
//        String encryptedPassword = passwordEncoder.encode(member.getPassword());
//        member.setPassword(encryptedPassword);

        return memberRepository.save(member);
    }


    /**
     * 이메일 중복 확인 메소드. 이메일 존재시 예외 발생
     */
    public void verifyNotExistEmail(String email) {
        Optional<Member> optionalEmail = memberRepository.findByEmail(email);
        if (optionalEmail.isPresent()) throw new BusinessLogicException(ExceptionCode.EMAIL_EXIST);
    }

}
