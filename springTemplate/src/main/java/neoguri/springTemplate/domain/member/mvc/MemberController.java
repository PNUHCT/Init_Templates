package neoguri.springTemplate.domain.member.mvc;

import lombok.RequiredArgsConstructor;
import neoguri.springTemplate.domain.member.dto.MemberPatchReqDto;
import neoguri.springTemplate.domain.member.dto.MemberPostReqDto;
import neoguri.springTemplate.domain.member.entity.Member;
import neoguri.springTemplate.dto.SingleResDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    /**
     * Post 요청
     * @return "data" : "String"
     */
    @PostMapping
    public ResponseEntity<SingleResDto<Long>> postMember (@RequestBody MemberPostReqDto memberPostReqDto) {
        Member savedMember = memberService.createMember(memberPostReqDto.toEntity());

        return new ResponseEntity<>(new SingleResDto<>(savedMember.getMemberId()), HttpStatus.CREATED);
    }


    /**
     * Patch 요청
     * @return "data" : "String"
     * @param memberPatchReqDto : 요청 Body
     * @return void
     */
    @PatchMapping("/edit/{memberId}")
    public ResponseEntity<SingleResDto<String>> patchMember (@RequestBody MemberPatchReqDto memberPatchReqDto,
                                                             @RequestParam Long memberId) {
        memberService.modifyMember(memberPatchReqDto.toEntity(), memberId);

        return new ResponseEntity<>(new SingleResDto<>("Success Modify"), HttpStatus.OK);
    }

}
