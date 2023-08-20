package neoguri.springTemplate.oauth2.kakao;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import neoguri.springTemplate.domain.member.entity.Member;
import neoguri.springTemplate.domain.member.mvc.MemberService;
import neoguri.springTemplate.exception.dto.BusinessLogicException;
import neoguri.springTemplate.exception.exceptionCode.ExceptionCode;
import neoguri.springTemplate.security.util.JwtParcingUtil;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/kakao")
public class KakaoOauthController {

    private final KakaoService kakaoService;
    private final MemberService memberService;
    private final JwtParcingUtil jwtParcingUtil;

    /**
     * 프론트 요청 API : 인증 code 받기용
     * @return redirect url for kakao Authorization
     */
    @GetMapping("/oauth")
    public ResponseEntity<?> kakaoConnect() throws UnsupportedEncodingException {

        return new ResponseEntity<>(kakaoService.createKakaoURL(), HttpStatus.OK); // 프론트 브라우저로 보내는 주소(프론트에서 받아서 리다이렉트 시키면, 인가코드를 받을 수 있다.)
    }


    /**
     * 카카오 callback API : 토큰 발급 및 서비스 멤버 생성
     * @param code 카카오 인증 code (프론트에서 카카오로부터 받아서 이 API에 담아서 전달해주면 됨)
     * @return Success Login message
     * @throws JsonProcessingException
     */
    @GetMapping("/callback")
    public String kakaoLogin(@RequestParam("code") String code, HttpServletResponse response) throws JsonProcessingException {
        kakaoService.loginKakao(code, response);

        return response.getHeader("Authorization") == null ? "Fail Login: User" :  "Success Login: User";
    }


    /**
     * Input Parameter가 AccessToken일 경우 : 해당 토큰에 한해서 로그아웃 (특정 기기만 로그아웃)
     * Input Parameter가 Admin key일 경우 : 해당사용자의 모든 토큰 만료처리 (모든 기기 로그아웃)
     * @param request 로그인 한 유저를 찾기 위함
     * @return 성공시 Success Logout | 실패시 예외 처리
     */
    @GetMapping("/logout")
    public ResponseEntity<?> kakaoLogout (HttpServletRequest request) {

        Long memberId = jwtParcingUtil.extractMemberIdFromJwt(request);
        Member loginMember = memberService.findMember(memberId);
        String accessToken = jwtParcingUtil.extractAccessTokenFromJwt(request);
        Long expiration = jwtParcingUtil.getExpiration(accessToken);

        RestTemplate restTemplate = new RestTemplate(); // Http 요청을 보내기 위한 템플릿 클래스
        HttpHeaders userHttpHeaders = new HttpHeaders(); // Http 요청을 위한 Headers
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>(); // Http 요청을 위한 parameters를 설정해주기 위한 클래스
        HttpEntity<MultiValueMap<String, String>> kakaoLogoutRequest = new HttpEntity<>(params, userHttpHeaders); // http 요청을 위한 엔티티 클래스 (Header와 Parans를 담아줌)

        userHttpHeaders.add("Authorization", "Bearer " + loginMember.getOauthAccessToken());  // "KakaoAk " + getKakaoAppKey());
        userHttpHeaders.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        params.add("target_id_type", "user_id");
        params.add("target_id", loginMember.getOauthId());

        try {
            ResponseEntity<String> LogoutResponse = restTemplate.exchange(
                    "https://kapi.kakao.com/v1/user/logout",
                    HttpMethod.POST,
                    kakaoLogoutRequest,
                    String.class
            );
            System.out.println(LogoutResponse);

            memberService.verifyMemberFromRedis(memberId, accessToken, expiration);  // 자체 서비스 로그아웃 로직

        } catch (Exception e) { throw new BusinessLogicException(ExceptionCode.NOT_FOUND); }

        return new ResponseEntity<>("Success Logout: User", HttpStatus.OK);
    }
}
