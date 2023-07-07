package neoguri.springTemplate.domain.member.dto;

import lombok.Getter;
import lombok.Setter;
import neoguri.springTemplate.domain.member.entity.Member;

@Getter @Setter
public class MemberPostReqDto {

    private String email;

    private String password;

    private String nickname;

    private String profile;

    public Member toEntity() {
        return new Member().builder()
                .email(this.email)
                .password(this.password)
                .nickname(this.nickname)
                .profile(this.profile)
                .memberStatus(Member.MemberStatus.MEMBER_ACTIVE)
                .build();
    }
}