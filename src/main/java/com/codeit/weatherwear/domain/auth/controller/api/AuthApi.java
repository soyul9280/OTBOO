package com.codeit.weatherwear.domain.auth.controller.api;

import com.codeit.weatherwear.domain.auth.dto.ResetPasswordRequest;
import com.codeit.weatherwear.domain.security.dto.SignInRequest;
import com.codeit.weatherwear.global.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "인증 관리", description = "인증 관련 API")
@RequestMapping("/api/auth")
public interface AuthApi {

  @Operation(summary = "액세스 토큰 조회", description = "쿠키(refresh_token)에 저장된 리프레시 토큰으로 엑세스 토큰을 조회합니다.")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "인증 정보 조회 성공",
          content = @Content(schema = @Schema(implementation = String.class))),
      @ApiResponse(
          responseCode = "401",
          description = "인증 정보 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @GetMapping
  ResponseEntity<String> getMe(@CookieValue(value = "refresh_token") Cookie refreshToken);

  @Operation(summary = "토큰 재발급", description = "쿠키(refresh_token)에 저장된 리프레시 토큰으로 리프레시 토큰과 엑세스 토큰을 재발급합니다.")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "토큰 재발급 성공",
          content = @Content(schema = @Schema(implementation = String.class))),
      @ApiResponse(
          responseCode = "401",
          description = "토큰 재발급 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @PostMapping
  ResponseEntity<String> rotateToken(
      @CookieValue(value = "refresh_token") Cookie refreshToken, HttpServletResponse response);

  @Operation(summary = "비밀번호 초기화", description = "임시 비밀번호로 초기화 후 이메일로 전송합니다.")
  @ApiResponses({
      @ApiResponse(
          responseCode = "204",
          description = "비밀번호 초기화 성공",
          content = @Content(schema = @Schema())),
      @ApiResponse(
          responseCode = "404",
          description = "비밀번호 초기화 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @PostMapping
  ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request);

  @Operation(summary = "CSRF 토큰 조회", description = "CSRF 토큰을 조회합니다. 토큰은 쿠키(XSRF-TOKEN)에 저장됩니다.")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "CSRF 토큰 조회 성공",
          content = @Content(schema = @Schema(implementation = CsrfToken.class))),
      @ApiResponse(
          responseCode = "401",
          description = "CSRF 토큰 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @GetMapping
  ResponseEntity<CsrfToken> getCsrfToken(CsrfToken csrfToken);

  @Operation(summary = "로그인", description = "이메일과 비밀번호를 입력하여 로그인합니다.")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "로그인 성공",
          content = @Content(schema = @Schema(implementation = String.class))),
      @ApiResponse(
          responseCode = "401",
          description = "로그인 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @PostMapping
  void signIn(@RequestBody SignInRequest signInRequest);

  @Operation(summary = "로그아웃", description = "로그아웃합니다.")
  @ApiResponses({
      @ApiResponse(
          responseCode = "204",
          description = "로그아웃 성공",
          content = @Content(schema = @Schema())),
      @ApiResponse(
          responseCode = "401",
          description = "로그아웃 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @PostMapping
  void signOut();

}
