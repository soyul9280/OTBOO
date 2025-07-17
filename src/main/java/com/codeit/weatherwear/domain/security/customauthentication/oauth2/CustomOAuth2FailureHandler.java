package com.codeit.weatherwear.domain.security.customauthentication.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Qualifier("customOAuth2FailureHandler")
public class CustomOAuth2FailureHandler implements
    AuthenticationFailureHandler {

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException exception) throws IOException, ServletException {
    String errorMessage = "OAuth 로그인 실패";

    if (exception instanceof OAuth2AuthenticationException) {
      errorMessage = ((OAuth2AuthenticationException) exception)
          .getError().getDescription();
    }

    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    response.setContentType("application/json");
    response.getWriter().write("""
        {
          "status": 400,
          "message": "%s"
        }
        """.formatted(errorMessage));
  }

}
