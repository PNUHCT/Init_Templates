package neoguri.springTemplate.security.util;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import neoguri.springTemplate.exception.dto.BusinessLogicException;
import neoguri.springTemplate.exception.exceptionCode.ExceptionCode;
import neoguri.springTemplate.security.jwt.JwtTokenizer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;


/**
 * Header로 입력받은 Jwt의 Payloads 내 Claims를 파싱하여 Claims에 담겨있는 memberId(식별자) 혹은 email을 추출하는 유틸
 * 각 도메인 내 메소드에서 로그인 한 유저정보로 검증이 필요할 경우 DI를 통해 호출해서 사용할 수 있습니다.
 * feat. memberId, email은 JwtTokenizer를 통해 JWT 생성 시 Claims에 넣어주었기에 있습니다. 만약 JWT를 생성할 때 담아주지 않는다면 사용불가능 합니다.
 * 반대로, JWT 생성 당시 더 많은 정보를 넣어준다면, 해당 정보도 추출해 낼 수 있습니다.
 * feat. 필요시, payloads에 있는 subject로부터 파싱해서 정보를 추출하는 메소드도 구현 가능합니다.
 */
@RequiredArgsConstructor @Component
public class JwtParcingUtil {

    private final JwtTokenizer jwtTokenizer;

    private final RedisTemplate redisTemplate;

    /**
     * memberId(Entity 식별자)를 얻는 파싱 메소드
      * @param request HttpServlet에 담겨오는 HttpHeader를 받기위함
     * @return 회원 식별자
     */
    public Long extractMemberIdFromJwt (HttpServletRequest request) {
        if(!isLoginUser(request)) throw new BusinessLogicException(ExceptionCode.LOGIN_REQUIRED);

        try {
            String jws = request.getHeader("Authorization").replace("Bearer ", "");

            verifyLoginToken(jws);  // 로그아웃한 유저(Logout된 Authorization이 들어왔을 경우)에 대한 필터링 로직. 레디스 활용

            String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
            Map<String, Object> claims = jwtTokenizer.getClaims(jws, base64EncodedSecretKey).getBody();
            Object value = claims.get("memberId");
            if( value == null ) return extractById(claims); // 만약 다른 테이블에 존재하는 Id값에 대한 조회 케이스가 필요한 경우 활성화
            return Long.valueOf(String.valueOf(value));
        }

        catch (Exception e) { throw new BusinessLogicException(ExceptionCode.LOGIN_REQUIRED); }
    }


    /**
     * memberEmail을 얻는 파싱 메소드
     * @param request 위와 동일합니다.
     * @return email
     */
    public String extractEmailFromJwt (HttpServletRequest request) {
        if(!isLoginUser(request)) throw new BusinessLogicException(ExceptionCode.LOGIN_REQUIRED);

        try {
            String jws = request.getHeader("Authorization").replace("Bearer ", "");

            verifyLoginToken(jws);  // 로그아웃한 유저(Logout된 Authorization이 들어왔을 경우)에 대한 필터링 로직. 레디스 활용

            String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
            Map<String, Object> claims = jwtTokenizer.getClaims(jws, base64EncodedSecretKey).getBody();
            Object value = claims.get("email");
//            if ( value == null ) return extractByUsername(claims); // 만약 다른 테이블에 존재하는 email값에 대한 조회 케이스가 필요한 경우 활성화
            return String.valueOf(String.valueOf(value));
        }

        catch (Exception e) { throw new BusinessLogicException(ExceptionCode.LOGIN_REQUIRED); }
    }


    /**
     * Authorization이 헤더에 존재하는지 검증하는 메소드
     * 헤더에 Authorization이라는 키값 자체가 없거나, Authorization에 value가 없을 경우 false 반환 (비로그인 상태)
     * 헤더에 Authorization의 밸류값이 존재하면, true 반환 (로그인 상태)
     * @param request HttpServletRequest
     * @return T | F 검증 : 로그인한 유저와 비로그인 상태의 조건을 주기위해 DI할 메소드
     */
    public Boolean isLoginUser(HttpServletRequest request) {
        try{
            if(request.getHeader("Authorization")!=null) return true;
            else return false;
        } catch (Exception e) { return false; }
    }


    /**
     * Authorization에서 AccessToken를 parsing하여 반환하는 메소드
     * @param request
     * @return
     */
    public String extractAccessTokenFromJwt(HttpServletRequest request) {
        try { return request.getHeader("Authorization").replace("Bearer ", ""); }
        catch (Exception e) { throw new BusinessLogicException(ExceptionCode.LOGIN_REQUIRED); }
    }


    /**
     * AccessToken에서 만료시간을 얻어, 현재시간부터 언제까지 시간이 남았는지 반환하는 메소드
     * Redis에 저장된 Logout확인용 AccessToken의 Timeout 지정을 위함
     * @param accessToken
     * @return
     */
    public Long getExpiration (String accessToken) {
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
        Date expiration = Jwts.parserBuilder().setSigningKey(base64EncodedSecretKey).build().parseClaimsJws(accessToken).getBody().getExpiration();
        Long now = new Date().getTime();
        return (expiration.getTime() - now);
    }


    /**
     * extractMemberIdFromJwt 내 로직에서 사용
     * 만약 Claims 안에 담겨있는 Id의 키값이 memberId가 아닌 경우. 즉, member테이블이 아닌 다른 테이블에 존재하는 객체를 조회하길 원하는 경우에 적용
     * @param claims
     * @return memberId (여기서 member는 실제 테이블에서 member는 아니다.
     */
    private Long extractById (Map<String, Object> claims) {
        try {
            Object value = claims.get("id");
            Long memberId = Long.valueOf(String.valueOf(value));
            return memberId;
        }
        catch (Exception e) { throw new BusinessLogicException(ExceptionCode.LOGIN_REQUIRED); }
    }


    /**
     * extractEmailFromJwt 내 로직에서 사용
     * 만약 Claims 안에 담겨있는 email의 키값이 member의 email이 아닌 경우. 즉, member 테이블이 아닌 다른 테이블에 존재하는 객체를 조회하길 원하는 경우에 적용
     * @param claims
     * @return email
     */
    private String extractByUsername (Map<String, Object> claims) {
        try {
            Object value = claims.get("username");
            String email = String.valueOf(String.valueOf(value));
            return email;
        }
        catch (Exception e) { throw new BusinessLogicException(ExceptionCode.LOGIN_REQUIRED); }
    }

    private void verifyLoginToken(String accessToken) {
        if(redisTemplate.opsForValue().get(accessToken)!=null
                && redisTemplate.opsForValue().get(accessToken).toString().equals("Logout")) {
            throw new BusinessLogicException(ExceptionCode.LOGIN_REQUIRED);
        }
    }

}