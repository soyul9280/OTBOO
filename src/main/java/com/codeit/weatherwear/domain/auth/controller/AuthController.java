package com.codeit.weatherwear.domain.auth.controller;

import com.codeit.weatherwear.domain.auth.service.AuthService;
import com.codeit.weatherwear.domain.security.service.JwtSessionService;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtSessionService jwtSessionService;

    // 액세스 토큰 조회

    @GetMapping("/me")
    public ResponseEntity<String> getMe(@CookieValue(value = "refresh_token") Cookie refreshToken) {
        String accessToken = jwtSessionService.findAccessToken(refreshToken.getValue());
        return ResponseEntity.ok(accessToken);
    }

}
