package neoguri.springTemplate.domain.member.dto;

import lombok.Getter;
import lombok.Setter;
import neoguri.springTemplate.auditing.BaseTimeEntity;
import neoguri.springTemplate.domain.member.entity.Member;

@Getter @Setter
public class MemberResDto extends BaseTimeEntity {

    private Long memberId;

    private String email;

    private String nickname;

    private String profile;

    private String memberStatus;


    public MemberResDto(Member member) {
        this.memberId = member.getMemberId();
        this.email = member.getEmail();
        this.nickname = member.getNickname();
        this.profile = member.getProfile();
        this.memberStatus = member.getMemberStatus().getStatus();
        super.setCreatedAt(member.getCreatedAt());
        super.setModifiedAt(member.getModifiedAt());
    }
}


