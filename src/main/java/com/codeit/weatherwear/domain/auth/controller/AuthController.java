package com.codeit.weatherwear.domain.auth.controller;

import com.codeit.weatherwear.domain.auth.controller.api.AuthApi;
import com.codeit.weatherwear.domain.auth.dto.ResetPasswordRequest;
import com.codeit.weatherwear.domain.auth.dto.SignInRequest;
import com.codeit.weatherwear.domain.auth.service.AuthService;
import com.codeit.weatherwear.domain.security.dto.TokenRotationResult;
import com.codeit.weatherwear.domain.security.service.JwtSessionService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth")
public class AuthController implements AuthApi {

  private final AuthService authService;
  private final JwtSessionService jwtSessionService;

  // 액세스 토큰 조회
  @GetMapping("/me")
  public ResponseEntity<String> getMe(@CookieValue(value = "refresh_token") Cookie refreshToken) {
    String accessToken = jwtSessionService.findAccessToken(refreshToken.getValue());
    return ResponseEntity.ok(accessToken);
  }

  // 토큰 재발급
  @PostMapping("/refresh")
  public ResponseEntity<String> rotateToken(
      @CookieValue(value = "refresh_token") Cookie refreshToken, HttpServletResponse response) {
    TokenRotationResult result = jwtSessionService.rotateToken(refreshToken.getValue());
    // 리프레시 토큰을 쿠키로 다시 설정
    ResponseCookie cookie = ResponseCookie.from("refresh_token", result.refreshToken())
        .httpOnly(true)
        .secure(false)
        .path("/")
        .sameSite("Strict")
        .maxAge(30 * 24 * 60 * 60)
        .build();
    response.addHeader("Set-Cookie", cookie.toString());
    return ResponseEntity.ok(result.accessToken());
  }

  // 비밀번호 초기화
  @PostMapping("/reset-password")
  public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    authService.resetPassword(request);
    return ResponseEntity.noContent().build();
  }

  // CSRF 토큰 발급
  @GetMapping("/csrf-token")
  public ResponseEntity<CsrfToken> getCsrfToken(CsrfToken csrfToken) {
    return ResponseEntity.ok(csrfToken);
  }

  @PostMapping("/sign-in")
  public void signIn(SignInRequest signInRequest) {
    throw new UnsupportedOperationException("Only for Documentation");
  }

  @PostMapping("/sign-out")
  public void signOut() {
    throw new UnsupportedOperationException("Only for Documentation");
  }
}
