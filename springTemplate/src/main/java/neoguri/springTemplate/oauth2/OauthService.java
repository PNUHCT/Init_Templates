package neoguri.springTemplate.oauth2;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.SecureRandom;

@Service
public class OauthService {

    @Getter
    @Value("${oauth.naver.clientId}")
    private String naverClientId;

    @Getter
    @Value("${oauth.naver.clientSecret}")
    private String naverClientSecret;

    @Getter
    @Value("${jwt.refresh-token-prefix}")
    private String refreshPrefix;


    public String createNaverURL () throws UnsupportedEncodingException {
        StringBuffer url = new StringBuffer();

        // 카카오 API 명세에 맞춰서 작성
        String redirectURI = URLEncoder.encode("http://www.localhost:8080/naver/callback", "UTF-8"); // redirectURI 설정 부분
        SecureRandom random = new SecureRandom();
        String state = new BigInteger(130, random).toString();

        url.append("https://nid.naver.com/oauth2.0/authorize?response_type=code");
        url.append("&client_id=" + getNaverClientId());
        url.append("&state=" + state);
        url.append("&redirect_uri=" + redirectURI);

        /* 로그인 중 선택 권한 허용 URL로 redirect 문제 해결하기
           로그인 시도시, "현재 UYouBooDan은 개발 중 상태입니다. 개발 중 상태에서는 등록된 아이디만 로그인할 수 있습니다." 화면으로 가버림.
           아래와 같은 URL로 리다이렉트 되도록 유도하는 해결책 찾기
           : https://nid.naver.com/oauth2.0/authorize?client_id=avgLtiDUfWMFfHpplTZh&redirect_uri=https://developers.naver.com/proxyapi/forum/auth/oAuth2&response_type=code&state=RZ760w
         */

        return url.toString();
    }

}
