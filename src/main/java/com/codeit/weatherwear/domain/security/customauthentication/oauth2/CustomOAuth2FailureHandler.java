package com.codeit.weatherwear.domain.security.customauthentication.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
@Qualifier("customOAuth2FailureHandler")
public class CustomOAuth2FailureHandler implements
    AuthenticationFailureHandler {

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException exception) throws IOException, ServletException {
    String errorMessage = "OAuth 로그인 실패";
    int status = HttpServletResponse.SC_UNAUTHORIZED;

    if (exception instanceof OAuth2AuthenticationException oauthEx) {
      OAuth2Error error = oauthEx.getError();
      errorMessage = error.getDescription();

      switch (error.getErrorCode()) {
        case "ACCOUNT_LOCKED" -> status = HttpServletResponse.SC_UNAUTHORIZED; // 잠금 계정이면 401
        case "USER_ALREADY_EXISTS" -> status = HttpServletResponse.SC_BAD_REQUEST; // 사용자 중복이면 400
        case "authorization_request_not_found" -> {
          status = HttpServletResponse.SC_BAD_REQUEST;
          errorMessage = "인증 요청 정보가 유실되었습니다. 다시 시도해주세요.";
        }
        default -> {
          status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
          errorMessage = "처리 중 문제가 생겼습니다. 다시 시도해주세요.";
          log.warn("Undefined OAuth2 SignIn ErrorCode: {}", error.getErrorCode());
        }
      }
    }

    response.setStatus(status);
    response.setContentType("application/json");
    response.getWriter().write("""
        {
          "status": %d,
          "message": "%s"
        }
        """.formatted(status, errorMessage));
  }

}
