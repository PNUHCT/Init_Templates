package neoguri.springTemplate.domain.member.dto;

import lombok.Getter;
import lombok.Setter;
import neoguri.springTemplate.domain.member.entity.Member;

@Getter @Setter
public class MemberPatchReqDto {

    private String password;
    private String nickname;
    private String profile;
    private Member.MemberStatus memberStatus;

    /**
     * 밸류지정방식
     */
    public Member toEntity() {
        return new Member().builder()
                .password(this.password)
                .nickname(this.nickname)
                .profile(this.profile)
                .memberStatus(this.memberStatus)
                .build();
    }
}