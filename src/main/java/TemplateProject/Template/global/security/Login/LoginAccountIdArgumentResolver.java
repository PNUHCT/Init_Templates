package TemplateProject.Template.global.security.Login;


import TemplateProject.Template.global.security.auth.dto.TokenPrincipalDto;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

// ArgumentResolver :  Controller로 들어온 파라미터를 가공하거나 수정 기능을 제공하는 객체
public class LoginAccountIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter (MethodParameter parameter) {
        boolean hasLoginAccountIdAnnotation = parameter.hasParameterAnnotation(LoginAccountId.class);
        boolean hasLongType = Long.class.isAssignableFrom(parameter.getParameterType());

        return hasLoginAccountIdAnnotation && hasLongType;
    }

    @Override
    public Object resolveArgument (MethodParameter parameter, ModelAndViewContainer mvcContainer,
                                   NativeWebRequest request, WebDataBinderFactory binderFactory) throws Exception {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();  // Spring Security의 Context 홀더 안에 있는 인증받은 객체 받아옴

        if (principal.equals("anonymousUser")) {  // 유저가 아닐경우, 식별자 -1L 처리
            return -1L;
        }

        TokenPrincipalDto castedPrincipal = (TokenPrincipalDto) principal;

        return castedPrincipal.getId();
    }
}
