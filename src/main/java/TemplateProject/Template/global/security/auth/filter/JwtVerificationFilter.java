package TemplateProject.Template.global.security.auth.filter;

import TemplateProject.Template.global.exception.exceptionCode.ExceptionCode;
import TemplateProject.Template.global.security.auth.dto.TokenPrincipalDto;
import TemplateProject.Template.global.security.auth.jwt.JwtTokenizer;
import TemplateProject.Template.global.security.auth.utils.CustomAuthorityUtils;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
public class JwtVerificationFilter extends OncePerRequestFilter {
    private final JwtTokenizer jwtTokenizer;
    private final CustomAuthorityUtils authorityUtils;

    protected void doFilterInternal (HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws IOException, ServletException {
        try {
            Map<String, Object> claims = verifyJws(request);
            setAuthenticationToContext(claims);
        }
        catch (ExpiredJwtException e) {
            request.setAttribute("exception", ExceptionCode.ACCESS_TOKEN_EXPIRATION);
        }
        catch (Exception e) {
            request.setAttribute("exception", e);
        }

        filterChain.doFilter(request, response);
    }

    private Map<String, Object> verifyJws (HttpServletRequest request) {
        String jws = request.getHeader("Authorization").replace("Bearer ", "");
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
        return jwtTokenizer.getClaims(jws, base64EncodedSecretKey).getBody();
    }

    private void setAuthenticationToContext(Map<String, Object> claims) {
        String email = (String) claims.get("email");
        long id = Long.valueOf((Integer) claims.get("id"));
        Collection<GrantedAuthority> authorities = authorityUtils.createAuthorities((String) claims.get("role"));
        Authentication authentication = new UsernamePasswordAuthenticationToken(new TokenPrincipalDto(id, email), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
