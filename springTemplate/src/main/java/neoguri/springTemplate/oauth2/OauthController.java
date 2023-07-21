package neoguri.springTemplate.oauth2;

import lombok.RequiredArgsConstructor;
import neoguri.springTemplate.domain.member.mvc.MemberService;
import neoguri.springTemplate.security.util.JwtParcingUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth")
public class OauthController {

    private final MemberService memberService;
    private final JwtParcingUtil jwtParcingUtil;
    private final OauthService oauthService;
    public ResponseEntity<?> getNaverUri() throws UnsupportedEncodingException {

        return new ResponseEntity<>(oauthService.createNaverURL(), HttpStatus.OK); // 프론트 브라우저로 보내는 주소
    }

}
