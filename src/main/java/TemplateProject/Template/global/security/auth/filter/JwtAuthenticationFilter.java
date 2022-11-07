package TemplateProject.Template.global.security.auth.filter;

import TemplateProject.Template.domain.account.entity.Account;
import TemplateProject.Template.global.common.dto.SingleResDto;
import TemplateProject.Template.global.security.Login.LoginDto;
import TemplateProject.Template.global.security.auth.jwt.JwtTokenizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenizer jwtTokenizer;

    @Override @SneakyThrows
    public Authentication attemptAuthentication (HttpServletRequest request, HttpServletResponse response) {
        ObjectMapper objectMapper = new ObjectMapper();
        LoginDto loginDto = objectMapper.readValue(request.getInputStream(), LoginDto.class);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                                                loginDto.getEmail(), loginDto.getPassword());
        return authenticationManager.authenticate(authenticationToken);
    }

    protected void successfulAuthentication (HttpServletResponse request, HttpServletResponse response,
                                             FilterChain filterChain, Authentication authResult) throws IOException {
        Account account = (Account) authResult.getPrincipal();

        String accessToken = jwtTokenizer.delegateAccessToken(account);           // 토큰 생성 부분
        String refreshToken = jwtTokenizer.delegateRefreshToken(account);

        response.setHeader("Authorization", "Bearer " + accessToken);  // 응답 헤더에 access token을 제공해 주도록 하는 부분

        String body = new Gson().toJson(new SingleResDto<>("success login"));      // 로그인시 보내는 응답 메세지 설정 부분 SingleResDto에 담아 전달함
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(body);
    }
}
