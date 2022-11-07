package TemplateProject.Template.global.security.auth.utils;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Collection;

@Component
public class CustomAuthorityUtils {
    public Collection<GrantedAuthority> createAuthorities (String role) {   // 각 권한별 역할 부여해주는 유틸
        Collection<GrantedAuthority> authorities = new ArrayDeque<>();
        authorities.add(() -> "ROLE_" + role);

        return authorities;
    }
}
