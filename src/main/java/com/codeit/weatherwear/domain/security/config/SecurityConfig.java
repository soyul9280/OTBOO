package com.codeit.weatherwear.domain.security.config;

import com.codeit.weatherwear.domain.security.customauthentication.CustomAuthenticationFailureHandler;
import com.codeit.weatherwear.domain.security.customauthentication.CustomAuthenticationFilter;
import com.codeit.weatherwear.domain.security.customauthentication.CustomAuthenticationSuccessHandler;
import com.codeit.weatherwear.domain.security.customauthentication.CustomUserDetailsService;
import com.codeit.weatherwear.domain.security.customauthentication.jwt.JwtAuthenticationFilter;
import com.codeit.weatherwear.domain.security.customauthentication.jwt.JwtLogoutHandler;
import com.codeit.weatherwear.domain.security.service.JwtSessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    @Profile("!test")
    SecurityFilterChain chain(HttpSecurity httpSecurity,
        CustomAuthenticationFilter customAuthenticationFilter,
        JwtAuthenticationFilter jwtAuthenticationFilter,
        JwtLogoutHandler jwtLogoutHandler) throws Exception {

        httpSecurity
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/users").permitAll() //회원가입
                // TODO: 일단은 모든 요청 허용
                .anyRequest().permitAll()
            )
            .addFilterAt(customAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(jwtAuthenticationFilter, CustomAuthenticationFilter.class)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // TODO: csrf 활성화
            .csrf(csrf -> csrf.disable())
            .logout(logout -> logout
                .logoutRequestMatcher(
                    new AntPathRequestMatcher("/api/auth/sign-out"))
                .logoutSuccessUrl("/") // 홈으로
                .deleteCookies("refresh-token")    // 쿠키 삭제
                .addLogoutHandler(jwtLogoutHandler) // JwtSession 삭제 & 토큰 블랙리스트 추가 핸들러
            );
        ;
        return httpSecurity.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
        CustomUserDetailsService customUserDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    @Bean
    public CustomAuthenticationFilter customAuthenticationFilter(
        ObjectMapper objectMapper,
        AuthenticationSuccessHandler authenticationSuccessHandler,
        AuthenticationFailureHandler authenticationFailureHandler,
        AuthenticationManager authenticationManager) {

        CustomAuthenticationFilter filter = new CustomAuthenticationFilter(objectMapper);
        // handler 설정
        filter.setAuthenticationSuccessHandler(authenticationSuccessHandler);
        filter.setAuthenticationFailureHandler(authenticationFailureHandler);
        // /api/auth/sign-in 경로에 적용
        filter.setFilterProcessesUrl("/api/auth/sign-in");
        // authenticationManager 지정
        filter.setAuthenticationManager(authenticationManager);
        return filter;
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler(
        ObjectMapper objectMapper, JwtSessionService jwtSessionService) {
        return new CustomAuthenticationSuccessHandler(objectMapper, jwtSessionService);
    }

    @Bean
    public AuthenticationFailureHandler customAuthenticationFailureHandler(
        ObjectMapper objectMapper) {
        return new CustomAuthenticationFailureHandler(objectMapper);
    }
}
