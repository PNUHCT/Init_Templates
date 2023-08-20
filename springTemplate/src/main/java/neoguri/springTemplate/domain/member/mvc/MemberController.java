package neoguri.springTemplate.domain.member.mvc;

import lombok.RequiredArgsConstructor;
import neoguri.springTemplate.domain.member.dto.MemberPatchReqDto;
import neoguri.springTemplate.domain.member.dto.MemberPostReqDto;
import neoguri.springTemplate.domain.member.dto.MemberResDto;
import neoguri.springTemplate.domain.member.entity.Member;
import neoguri.springTemplate.dto.MultiResDto;
import neoguri.springTemplate.dto.SingleResDto;
import neoguri.springTemplate.security.util.JwtParcingUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private final JwtParcingUtil jwtParcingUtil;

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
    @PatchMapping("/edit")
    public ResponseEntity<SingleResDto<String>> patchMember (@RequestBody MemberPatchReqDto memberPatchReqDto,
                                                             HttpServletRequest request) {
        Long memberId = jwtParcingUtil.extractMemberIdFromJwt(request);
        memberService.modifyMember(memberPatchReqDto.toEntity(), memberId);

        return new ResponseEntity<>(new SingleResDto<>("Success Modify"), HttpStatus.OK);
    }



    /**
     * Delete 요청
     * 애너테이션은 delete 요청을 받을 것이므로, DeleteMapping으로 받음
     * 상태변환을 할 예정이므로, HttpStatus는 NO_CONTENT가 아닌 OK로 함
     * 회원탈퇴(withdraw) 후 성공 메세지 반환
     * @return "data" : "성공 메세지"
     */
    @DeleteMapping("/remove")
    public ResponseEntity<SingleResDto<String>> withdrawMember (HttpServletRequest request) {
        memberService.withdrawMember(jwtParcingUtil.extractMemberIdFromJwt(request));

        return new ResponseEntity<>(new SingleResDto<>("정상적으로 탈퇴되었습니다."), HttpStatus.OK);
    }


    /**
     * 회원 삭제 메소드
     * @return 삭제 성공 메세지
     */
    @DeleteMapping("/delete")
    public ResponseEntity<SingleResDto<String>> deleteMember (HttpServletRequest request) {
        memberService.removeMember(jwtParcingUtil.extractMemberIdFromJwt(request));

        return new ResponseEntity<>(new SingleResDto<>("Success Delete"), HttpStatus.OK);
    }


    /**
     * 단일 Get 요청
     * @return "data" : "단일 객체에 대한 응답정보"
     */
    @GetMapping("/find")
    public ResponseEntity<SingleResDto<MemberResDto>> getMember (HttpServletRequest request) {
        Member member = memberService.findMember(jwtParcingUtil.extractMemberIdFromJwt(request));
        MemberResDto response = new MemberResDto(member);

        return new ResponseEntity<>(new SingleResDto<>(response), HttpStatus.OK);
    }


    /**
     * Page Get 요청
     * @return "data" : "String"
     */
    @GetMapping("/find-all")
    public ResponseEntity<MultiResDto<MemberResDto>> getMembers (Pageable pageable) {
        Page<Member> page = memberService.findMembers(pageable);
        Page<MemberResDto> response = page.map(MemberResDto::new);
        List<MemberResDto> list = response.stream().collect(Collectors.toList());

        return new ResponseEntity<>(new MultiResDto<>(list, page), HttpStatus.OK);
    }

}
