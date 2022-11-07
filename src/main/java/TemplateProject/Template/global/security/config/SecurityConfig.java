package TemplateProject.Template.global.security.config;

import TemplateProject.Template.global.security.auth.filter.JwtAuthenticationFilter;
import TemplateProject.Template.global.security.auth.filter.JwtVerificationFilter;
import TemplateProject.Template.global.security.auth.handler.AccountAccessDeniedHandler;
import TemplateProject.Template.global.security.auth.handler.AccountAuthenticationEntryPoint;
import TemplateProject.Template.global.security.auth.handler.AccountAuthenticationFailureHandler;
import TemplateProject.Template.global.security.auth.jwt.JwtTokenizer;
import TemplateProject.Template.global.security.auth.utils.CustomAuthorityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration @RequiredArgsConstructor
public class SecurityConfig {
    // Spring Security 설정. Authentication과 Authorization 설정하는 구간

    // 1. JWT를 이용한 JSON 로그인 방식 (폼 로그인 X)
    private final JwtTokenizer jwtTokenizer;
    private final CustomAuthorityUtils authorityUtils;

    public SecurityFilterChain filterChain (HttpSecurity http) throws Exception {
        return http
                .csrf().disable()           // 실제 서비스에선 disable 제거
                .cors().disable()           // 실제 서비스에선 disable 제거

                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // 세션 생성 제어(현재는 생성하지 않도록 함)

                .and()
                .formLogin().disable()
                .httpBasic().disable()
                .exceptionHandling()
                .authenticationEntryPoint(new AccountAuthenticationEntryPoint())
                .accessDeniedHandler(new AccountAccessDeniedHandler())

                .and()
                .apply(new CustomFilterConfigurer())

                .and()
                .authorizeHttpRequests(authorize -> authorize
                        .mvcMatchers(HttpMethod.POST, "/accounts").permitAll()
                        .mvcMatchers("/accounts/user").authenticated()
                        .mvcMatchers(HttpMethod.GET, "/**").permitAll()
                        .anyRequest().authenticated()
                )
                .build();
    }

    public class CustomFilterConfigurer extends AbstractHttpConfigurer<CustomFilterConfigurer, HttpSecurity> {  // 커스텀 필터 설정
        @Override
        public void configure (HttpSecurity builder) throws Exception {
            AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);
            JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager, jwtTokenizer);
            jwtAuthenticationFilter.setFilterProcessesUrl("auth/login");   // 로그인 요청 URL 설정
            jwtAuthenticationFilter.setAuthenticationFailureHandler(new AccountAuthenticationFailureHandler());

            JwtVerificationFilter jwtVerificationFilter = new JwtVerificationFilter(jwtTokenizer, authorityUtils);

            builder
                    .addFilter(jwtAuthenticationFilter)
                    .addFilterAfter(jwtVerificationFilter, JwtAuthenticationFilter.class);
        }
    }


    // 1. 시큐리티 내부에서 Id/password 지정해주는 방식 --------------------------------
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf().disable()
//                .formLogin()
//                .loginPage("/auths/login-form")                           // 로그인 Path
//                .loginProcessingUrl("/process_login")
//                .failureUrl("/auths/login-form?error")  // 로그인 실패시 리다이렉팅 할 URL Path
//                .and()
//                .logout()                                                 // 로그아웃 기능을 추가하기 위해 호출
//                .logoutUrl("/logout")                                     // 로그아웃 request URL. header.html의 로그아웃 메뉴에 지정한 href="/logout"경로와 동일해야함
//                .logoutSuccessUrl("/")                                    // 리다이렉트할 URL
//                .and()
//                .exceptionHandling().accessDeniedPage("auth/access-denied")
//                .and()
//                .authorizeHttpRequests(authorize -> authorize
//                        .antMatchers("/orders/**").hasRole("ADMIN")
//                        .antMatchers("members/my-page").hasRole("USER")
//                        .antMatchers("/**").permitAll()
//
//                  );
//        return http.build();
//    }
//
//    @Bean
//    public UserDetailsManager userDetailsService() {  // 스프링 Sequrity의 UserDetailsManager import
//        // 유저를 자유롭게 추가할 수 있다.
//        UserDetails admin =
//                User.withDefaultPasswordEncoder()
//                        .username("nyong9221@naver.com")
//                        .password("asdf1234")
//                        .roles("ADMIN")
//                        .build();
//
//        UserDetails user =
//                User.withDefaultPasswordEncoder()
//                        .username("nyong9221@gmail.com")
//                        .password("1111")
//                        .roles("USER")
//                        .build();
//
//        return new InMemoryUserDetailsManager(user);
//    }
}
