package com.codeit.weatherwear.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("test")
@EnableMethodSecurity
public class TestSecurityConfig {

  // 테스트에서는 JWT, 커스텀 필터가 필요하지 않으므로 단순하게 작성
  @Bean
  public SecurityFilterChain noSecurityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable()) // CSRF 토큰 보호 비활성화 -> CSRF 처리 실패로 인한 403 응답 방지
        .authorizeHttpRequests(
            auth -> auth.anyRequest().authenticated()) // 모든 요청에 인증이 필요함 -> UserDetailService 정의로 만족
        .httpBasic(
            Customizer.withDefaults()) // 간단한 인증 방식 사용 -> TestRestTemplate.withBasicAuth() 메서드 사용 가능
    ;

    return http.build();
  }

  // 테스트 전용 사용자 정보 저장소 등록
  @Bean
  public UserDetailsService userDetailsService() {
    // 가짜 인증 객체 설정
    UserDetails user = User.withUsername("user")
        .password("password") // NoOpPasswordEncoder를 사용하므로 암호화하지 않고 사용
        .roles("ADMIN") // 관리자 권한 -> 모든 테스트에서 인가 통과 가능
        .build();
    return new InMemoryUserDetailsManager(user);  // 메모리에 저장
  }

  // 인증을 수행할 AuthenticationManager 등록
  @Bean
  public AuthenticationManager authenticationManager(UserDetailsService userDetailsService) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return new ProviderManager(provider);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    // 인코딩을 하지 않는 NoOpPasswordEncoder 사용
    // 테스트용, 절대 운영에 사용 X
    return NoOpPasswordEncoder.getInstance();
  }
}

