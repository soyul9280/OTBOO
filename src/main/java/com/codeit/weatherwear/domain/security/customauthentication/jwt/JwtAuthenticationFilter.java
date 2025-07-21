package com.codeit.weatherwear.domain.security.customauthentication.jwt;

import com.codeit.weatherwear.domain.security.customauthentication.CustomUserDetails;
import com.codeit.weatherwear.domain.security.service.JwtSessionService;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import com.codeit.weatherwear.global.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Profile("!test")
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtSessionService jwtSessionService;
  private final UserRepository userRepository;

  private final ObjectMapper objectMapper;
  private static final String BEARER_PREFIX = "Bearer ";


  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    // Authorization 헤더에서 jwt access token 추출
    String token = extractTokenFromHeader(request);

    // 토큰 유효성 & 로그인 상태 검증
    if (token != null && jwtSessionService.isValidToken(token) && jwtSessionService.isSignedIn(
        token)) {
      // 인증 처리
      UUID userId = jwtSessionService.extractUserId(token);

      User user = userRepository.findById(userId).orElseThrow(
          () -> new UsernameNotFoundException("User not found with id: " + userId));
      CustomUserDetails userDetails = new CustomUserDetails(
          userId,
          user.getEmail(),
          user.getPassword(),
          user.getRole(),
          user.isLocked(),
          user.getTempPasswordExpirationTime()
      );
      UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
          userDetails, null, userDetails.getAuthorities());
      SecurityContextHolder.getContext().setAuthentication(auth);
      // 필터 처리 계속
      filterChain.doFilter(request, response);

    } else if (token != null) {
      // 잘못된 토큰 & 비로그인 상태 -> 401 응답
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");
      ErrorResponse errorResponse = new ErrorResponse("JwtValidationException", null, null);
      objectMapper.writeValue(response.getWriter(), errorResponse);
    } else {
      // 토큰이 없는 경우
      log.debug("JWT Token doesn't exist");
      filterChain.doFilter(request, response);
    }

  }

  private String extractTokenFromHeader(HttpServletRequest request) {
    // AUTHORIZATION 헤더에서 추출
    String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
    // "Bearer " 제거
    if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
      return bearerToken.substring(BEARER_PREFIX.length());
    }
    return null;
  }

}
