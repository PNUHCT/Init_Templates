package neoguri.springTemplate.oauth2.google;

import lombok.RequiredArgsConstructor;
import neoguri.springTemplate.domain.member.mvc.MemberService;
import neoguri.springTemplate.exception.dto.BusinessLogicException;
import neoguri.springTemplate.exception.exceptionCode.ExceptionCode;
import neoguri.springTemplate.security.util.JwtParcingUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
@RestController
@RequestMapping("/google")
public class GoogleOauthController {

    private final GoogleService googleService;
    private final MemberService memberService;
    private final JwtParcingUtil jwtParcingUtil;

    /**
     * 프론트 요청 API : 인증 code 받기용
     * @return redirect url for Google Authorization
     */
    @GetMapping("/oauth")
    public ResponseEntity<?> googleConnect() {

        return new ResponseEntity<>(googleService.createGoogleURL(), HttpStatus.OK);
    }


    /**
     *
     * 구글 callback API : 토큰 발급 및 서비스 멤버 생성
     * @param code 구글 인증 code (프론트에서 구글로부터 받아서 이 API에 담아서 전달해주면 됨)
     * @return Success Login message
     */
    @GetMapping("/callback")
    public String redirectGoogleLogin(@RequestParam("code") String code, HttpServletResponse response) {
        googleService.loginGoogle(code, response);

        return response.getHeader("Authorization") == null ? "Fail Login: User" : "Success Login: User";
    }


    /**
     * 구글 로그 아웃
     * @param request
     * @return
     */
    @GetMapping("/logout")
    public ResponseEntity<?> googleLogout(HttpServletRequest request) {

        Long memberId = jwtParcingUtil.extractMemberIdFromJwt(request);
        String accessToken = jwtParcingUtil.extractAccessTokenFromJwt(request);
        Long expiration = jwtParcingUtil.getExpiration(accessToken);

        try {
            // 구글 토큰 만료 시키기 위한 로직 추가 필요
            memberService.verifyMemberFromRedis(memberId, accessToken, expiration);  // 자체 서비스 로그아웃 로직

        } catch (Exception e) { throw new BusinessLogicException(ExceptionCode.NOT_FOUND); }


        return new ResponseEntity<>("Success Logout: User", HttpStatus.OK);
    }
}
