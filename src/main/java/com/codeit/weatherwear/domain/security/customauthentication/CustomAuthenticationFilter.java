package com.codeit.weatherwear.domain.security.customauthentication;

import com.codeit.weatherwear.domain.security.dto.SignInRequest;
import com.codeit.weatherwear.global.exception.ErrorCode;
import com.codeit.weatherwear.global.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  private final ObjectMapper objectMapper;

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
      HttpServletResponse response) throws AuthenticationException {

    // Request Body에서 email, password 추출
    try {
      SignInRequest signInRequest = objectMapper.readValue(request.getInputStream(),
          SignInRequest.class);
      String email = signInRequest.email();
      String password = signInRequest.password();

      // 입력 값 검증
      if (email == null || password == null) {
        writeBadRequestResponse(response);
        return null;
      }

      // Authentication 객체 생성
      UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
          email, password);

      // request에서 IP 등을 추출해서 authenticationToken.setDetails()로 인증 객체에 부가 정보를 넣음
      setDetails(request, authenticationToken);

      return this.getAuthenticationManager().authenticate(authenticationToken);

    } catch (IOException e) {
      writeBadRequestResponse(response);
      return null;
    }
  }

  private void writeBadRequestResponse(HttpServletResponse response) {
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    ErrorCode errorCode = ErrorCode.INVALID_SIGN_IN_REQUEST;
    ErrorResponse error = ErrorResponse.builder()
        .exceptionName(errorCode.name())
        .message(errorCode.getMessage())
        .details(Map.of("reason", errorCode.getDetail()))
        .build();

    try {
      objectMapper.writeValue(response.getWriter(), error);
    } catch (IOException ioException) {
      log.info("Fail to write response about invalid SignInRequest");
    }
  }
}
