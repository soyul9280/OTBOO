package com.codeit.weatherwear.domain.auth.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.weatherwear.domain.auth.dto.ResetPasswordRequest;
import com.codeit.weatherwear.domain.auth.service.AuthService;
import com.codeit.weatherwear.domain.security.dto.TokenRotationResult;
import com.codeit.weatherwear.domain.security.service.JwtSessionService;
import com.codeit.weatherwear.global.base.BaseControllerTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(AuthController.class)
class AuthControllerTest extends BaseControllerTest {

  @MockitoBean
  private AuthService authService;
  @MockitoBean
  private JwtSessionService jwtSessionService;
  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("쿠키의 리프레시 토큰으로 액세스 토큰을 조회한다.")
  void getMe_shouldReturnAccessToken() throws Exception {
    // given
    String refreshTokenValue = "refresh-token";
    String accessToken = "access-token";

    given(jwtSessionService.findAccessToken(refreshTokenValue)).willReturn(accessToken);

    // when & then
    mockMvc.perform(get("/api/auth/me")
            .cookie(new Cookie("refresh_token", refreshTokenValue)))
        .andExpect(status().isOk())
        .andExpect(content().string(accessToken));
  }

  @Test
  @DisplayName("리프레시 토큰과 액세스 토큰을 재발급한다.")
  void rotateToken_shouldReturnNewToken() throws Exception {
    // given
    String oldRefreshToken = "old-refresh-token";
    String newRefreshToken = "new-refresh-token";
    String newAccessToken = "new-access-token";

    given(jwtSessionService.rotateToken(oldRefreshToken)).willReturn(
        new TokenRotationResult(newAccessToken, newRefreshToken));

    // when & then
    mockMvc.perform(post("/api/auth/refresh")
            .cookie(new Cookie("refresh_token", oldRefreshToken)))
        .andExpect(status().isOk())
        .andExpect(content().string(newAccessToken))
        .andExpect(cookie().value("refresh_token", newRefreshToken));
  }

  @Test
  @DisplayName("비밀번호 초기화 요청 시 NoContent status")
  void resetPassword() throws Exception {
    // given
    ResetPasswordRequest request = new ResetPasswordRequest("test@mail.com");
    doNothing().when(authService).resetPassword(request);

    // when & then
    mockMvc.perform(post("/api/auth/reset-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());
  }


  @Test
  void getCsrfToken_shouldReturnCsrfToken() throws Exception {
    CsrfToken csrfToken = new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "csrf-token-value");

    mockMvc.perform(get("/api/auth/csrf-token")
            .requestAttr(CsrfToken.class.getName(), csrfToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").exists())
        .andExpect(jsonPath("$.parameterName").value("_csrf"))
        .andExpect(jsonPath("$.headerName").value("X-CSRF-TOKEN"));
  }
}