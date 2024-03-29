package neoguri.springTemplate.domain.member.entity;

import lombok.*;
import neoguri.springTemplate.auditing.BaseTimeEntity;
import neoguri.springTemplate.exception.dto.BusinessLogicException;
import neoguri.springTemplate.exception.exceptionCode.ExceptionCode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Entity
public class Member extends BaseTimeEntity {

    @Id
    @Setter // 시큐리티 MemberDetailsService 내 MemberDetails생성을 위해 적용 (추후 변경 검토)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Setter // 시큐리티 MemberDetailsService 내 MemberDetails생성을 위해 적용 (추후 변경 검토)
    private String email;

    @Setter
    private String password;

    private String nickname;

    private String profile;

    private String oauthId;

    private String oauthAccessToken;

    @Enumerated(value = EnumType.STRING)
    @Column(length = 20, nullable = false)  // 컬럼설정 지정 안할시 오류 발생 => Unable to instantiate custom type: org.hibernate.type.EnumType 에러발생
    private MemberStatus memberStatus;

    /**
     * Member 객체에 대한 역할 테이블을 자동 생생 후 역할을 부여 (시큐리티를 통해 작동)
     */
    @Setter // 시큐리티 MemberDetailsService 내 MemberDetails생성을 위해 적용 (추후 변경 검토)
    @ElementCollection(fetch = FetchType.EAGER)
    List<String> roles = new ArrayList<>();


    /**
     * 회원상태 Enum관리 : default값은 MEMBER_ACTIVE로 필드에서 지정
     */
    @NoArgsConstructor
    public enum MemberStatus {
        MEMBER_ACTIVE("활동중"),
        MEMBER_SLEEP("휴면계정"),
        MEMBER_QUIT("탈퇴 계정");

        @Getter
        private String status;

        MemberStatus(String status) {
            this.status = status;
        }
    }


    /**
     * 기본 프로필 지정
     */
    public void defaultProfile() {
        if(this.profile == null)
        this.profile = "https://scontent-gmp1-1.xx.fbcdn.net/v/t1.18169-9/527016_499021583525593_732357164_n.jpg?_nc_cat=107&ccb=1-7&_nc_sid=09cbfe&_nc_ohc=CdbnqyyFWXkAX_obHCp&_nc_ht=scontent-gmp1-1.xx&oh=00_AfDBQSsLnCXMKoAPnGFrOeSBgvMp__vgjXLEqmtS6etfcw&oe=63F1DB6A";
    }


    /**
     * 비즈니스 로직 숨기기용도 (feat.DDD)
     */
    public Member verifyMember(Optional<Member> optMember) {
        return optMember.orElseThrow(()->new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
    }

    public void modifyEmail(String email) {this.email = email;}
    public void modifyPassword(String password){ if(password != null) this.password = password; }
    public void modifyNickname(String nickname){ if(nickname!=null) this.nickname = nickname; }
    public void modifyProfile(String profile){ if(profile!=null) this.profile = profile; }
    public void modifyMemberStatus(MemberStatus status){ this.memberStatus = status; }
    public void withdrawMember(){ this.memberStatus = MemberStatus.MEMBER_QUIT; }
    public void modifyOauthToken(String oauthAccessToken){ this.oauthAccessToken = oauthAccessToken; }
}
