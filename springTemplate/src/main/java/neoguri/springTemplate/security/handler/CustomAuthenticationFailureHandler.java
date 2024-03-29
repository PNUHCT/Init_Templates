package neoguri.springTemplate.security.handler;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import neoguri.springTemplate.exception.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * 존재하지 않는 계정일 경우와, 비밀번호가 틀린 경우 모두 진입함.
 */
@Slf4j
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure (HttpServletRequest request, HttpServletResponse response,
                                         AuthenticationException exception) throws IOException {

        log.info("Authentication failed: {}", exception.getMessage());
        sendErrorResponse(response);

    }

    private void sendErrorResponse(HttpServletResponse response) throws IOException {
        Gson gson = new Gson();
        ErrorResponse errorResponse = ErrorResponse.of(HttpStatus.UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(gson.toJson(errorResponse, ErrorResponse.class));
    }
}
