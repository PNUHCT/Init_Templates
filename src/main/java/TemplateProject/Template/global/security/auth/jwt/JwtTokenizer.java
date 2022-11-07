package TemplateProject.Template.global.security.auth.jwt;

import TemplateProject.Template.domain.account.entity.Account;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenizer {

    @Getter @Value("${jwt.key.secret}")  // import주의! 롬복 아님. base64EncodedSecretKey에 전달하기 위해 Getter 사용
    private String secretKey;

    @Getter @Value("${jwt.access-token-expiration-minutes}")
    private int accessTokenExpirationMinutes;

    @Getter @Value("${jwt.refresh-token-expiration-minutes}")
    private int refreshTokenExpirationMinutes;

    public String encodeBase64SecretKey(String secretKey) {
        return Encoders.BASE64.encode(secretKey.getBytes(StandardCharsets.UTF_8));  // 비밀키 인코딩 (UTF_8)
    }

    public String generateAccessToken (Map<String, Object> claims, String subject, Date expiration, String base64EncodedSecretKey) {
        Key key = getKeyFromBase64EncodedKey(base64EncodedSecretKey);  // base64로 인코딩된 비밀키를 얻음

        return Jwts.builder()
                .setClaims(claims)                                 // claim : 사용자에 대한 프로퍼티나 속성
                .setSubject(subject)                               // subject : 토큰 제목
                .setIssuedAt(Calendar.getInstance().getTime())     // 발행일
                .setExpiration(expiration)                         // 만료일
                .signWith(key)                                     // 인코딩된 비밀키
                .compact();
    }

    public String generateRefreshToken (String subject, Date expiration, String base64EncodedSecretKey) {
        Key key = getKeyFromBase64EncodedKey(base64EncodedSecretKey);

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(Calendar.getInstance().getTime())
                .setExpiration(expiration)
                .signWith(key)
                .compact();
    }

    private Key getKeyFromBase64EncodedKey (String base64EncodedSecretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(base64EncodedSecretKey);      // io import

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Jws<Claims> getClaims (String jws, String base64EncodedSecretKey) {  // Jws : JWT 웹 토큰
        Key key = getKeyFromBase64EncodedKey(base64EncodedSecretKey);

        return Jwts.parserBuilder()  // 파싱작업
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jws);
    }

    private Date getTokenExpiration (int expirationMinutes) {  // 토큰 만료시간 전달구간
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, expirationMinutes);

        return calendar.getTime();
    }

    public String delegateAccessToken (Account account) {    // Account 도메인과 연결하는 구간
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", account.getEmail());  // 유저Id
        claims.put("role", account.getRole());       // 유저 권한 등급
        claims.put("id", account.getId());           // 유저 식별자

        String subject = String.valueOf(account.getId());
        Date expiration = getTokenExpiration(getAccessTokenExpirationMinutes());
        String base64EncodedSecretKey = encodeBase64SecretKey(getSecretKey());

        return generateAccessToken(claims, subject, expiration, base64EncodedSecretKey);  // Account에 AccessToken 생성!
    }

    public String delegateRefreshToken (Account account) {
        String subject = String.valueOf(account.getId());
        Date expiration = getTokenExpiration(getRefreshTokenExpirationMinutes());
        String base64EncodedSecretKey = encodeBase64SecretKey(getSecretKey());

        return generateRefreshToken(subject, expiration, base64EncodedSecretKey);
    }
}
