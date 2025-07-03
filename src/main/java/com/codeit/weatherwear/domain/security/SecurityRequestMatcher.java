package com.codeit.weatherwear.domain.security;

import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;


/**
 * SecurityFilterChain에 허용할 경로 관리
 */
public class SecurityRequestMatcher {

  // "/api"로 시작하지 않는 요청 -> 정적 리소스 허용
  public static final RequestMatcher NON_API = new NegatedRequestMatcher(
      new AntPathRequestMatcher("/api/**"));

  // 회원가입
  public static final RequestMatcher SIGN_UP = new AntPathRequestMatcher("/api/users",
      HttpMethod.POST.name());

  // 로그인
  public static final RequestMatcher SIGN_IN = new AntPathRequestMatcher("/api/auth/sign-in",
      HttpMethod.POST.name());

  // 로그아웃
  public static final RequestMatcher SIGN_OUT = new AntPathRequestMatcher("/api/auth/sign-out");

  // 비밀번호 초기화
  public static final RequestMatcher RESET_PASSWORD = new AntPathRequestMatcher(
      "/api/auth/reset-password", HttpMethod.POST.name());

  // 토큰 재발급
  public static final RequestMatcher REFRESH = new AntPathRequestMatcher("/api/auth/refresh",
      HttpMethod.POST.name());
  
  public static final RequestMatcher[] PUBLIC_MATCHERS = new RequestMatcher[]{
      NON_API, SIGN_UP, SIGN_IN, SIGN_OUT, RESET_PASSWORD, REFRESH
  };
}
