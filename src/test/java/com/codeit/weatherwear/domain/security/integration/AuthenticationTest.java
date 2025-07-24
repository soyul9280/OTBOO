//package com.codeit.weatherwear.domain.security.integration;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//import static org.junit.jupiter.api.Assertions.assertNotEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//
//import com.codeit.weatherwear.domain.user.entity.Role;
//import com.codeit.weatherwear.domain.user.entity.User;
//import com.codeit.weatherwear.domain.user.repository.UserRepository;
//import com.codeit.weatherwear.global.config.ContainerInitializer;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.UUID;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.ContextConfiguration;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("test-security")
//@ContextConfiguration(initializers = ContainerInitializer.class)
//@Slf4j
//public class AuthenticationTest {
//
//  @Autowired
//  private ObjectMapper objectMapper;
//  @Autowired
//  private TestRestTemplate restTemplate;
//  @Autowired
//  private UserRepository userRepository;
//  @Autowired
//  private PasswordEncoder passwordEncoder;
//  UUID userId;
//
//  @BeforeEach
//  void setUp() {
//    User user = userRepository.save(
//        User.builder()
//            .email("test@mail.com")
//            .password(passwordEncoder.encode("password"))
//            .name("testname")
//            .role(Role.USER)
//            .build()
//    );
//    userId = user.getId();
//  }
//
//  @AfterEach
//  void tearDown() {
//    userRepository.deleteAll();
//  }
//
//  @Test
//  @DisplayName("로그인 성공 시 토큰이 응답 바디와 쿠키에 포함된다.")
//  void SignInSuccessShouldReturnToken() {
//    // given
//    Map<String, String> requestBody = Map.of(
//        "email", "test@mail.com",
//        "password", "password"
//    );
//    HttpHeaders headers = new HttpHeaders();
//    headers.setContentType(MediaType.APPLICATION_JSON);
//    HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
//
//    // when
//    ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/sign-in", request,
//        String.class);
//
//    // then
//    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//    assertNotNull(response.getBody());
//    List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
//    assertThat(cookies).isNotNull();
//
//    Optional<String> refreshCookie = cookies.stream()
//        .filter(cookie -> cookie.startsWith("refresh_token="))
//        .findFirst();
//
//    assertThat(refreshCookie).isPresent();
//  }
//
//  @Test
//  @DisplayName("유효한 리프레시 토큰이 있을 때 토큰 재발급 요청으로 새로운 토큰을 받는다.")
//  void refreshToken() {
//    /**
//     * 로그인 요청으로 유효한 토큰 먼저 얻기
//     */
//
//    // given
//    Map<String, String> loginBody = Map.of(
//        "email", "test@mail.com",
//        "password", "password"
//    );
//
//    HttpHeaders loginHeaders = new HttpHeaders();
//    loginHeaders.setContentType(MediaType.APPLICATION_JSON);
//
//    HttpEntity<Map<String, String>> loginRequest = new HttpEntity<>(loginBody, loginHeaders);
//
//    // when
//    ResponseEntity<String> loginResponse = restTemplate.postForEntity(
//        "/api/auth/sign-in", loginRequest, String.class
//    );
//
//    // then
//    assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
//
//    List<String> setCookies = loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE);
//    assertThat(setCookies).isNotNull();
//
//    String refreshTokenCookie = setCookies.stream()
//        .filter(cookie -> cookie.startsWith("refresh_token="))
//        .findFirst()
//        .orElseThrow(() -> new IllegalStateException("No refresh_token cookie found"));
//    String refreshTokenPair = refreshTokenCookie.split(";", 2)[0]; // refresh_token=... 형태로 추출
//
//    /**
//     * csrf 토큰 요청
//     */
//
//    HttpHeaders csrfHeaders = new HttpHeaders();
//
//    HttpEntity<Void> csrfRequest = new HttpEntity<>(null, csrfHeaders);
//
//    ResponseEntity<String> csrfResponse = restTemplate.exchange(
//        "/api/auth/csrf-token", HttpMethod.GET, csrfRequest, String.class
//    );
//
//    assertThat(csrfResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
//
//    List<String> csrfSetCookies = csrfResponse.getHeaders().get(HttpHeaders.SET_COOKIE);
//    assertThat(csrfSetCookies).isNotNull();
//
//    String xsrfTokenCookie = csrfSetCookies.stream()
//        .filter(cookie -> cookie.startsWith("XSRF-TOKEN="))
//        .findFirst()
//        .orElseThrow(() -> new IllegalStateException("No XSRF-TOKEN cookie found"));
//
//    String xsrfTokenPair = xsrfTokenCookie.split(";", 2)[0];  // xsrf_token=... 형태로 추출
//    String xsrfTokenValue = xsrfTokenPair.split("=")[1];  // 토큰값만 추출
//
//    /**
//     * 토큰 재발급 요청
//     */
//    // given
//    // /api/auth/refresh 요청에 쿠키와 헤더 포함
//    HttpHeaders refreshHeaders = new HttpHeaders();
//    refreshHeaders.set("Cookie", refreshTokenPair + "; " + xsrfTokenPair);
//    refreshHeaders.set("X-XSRF-TOKEN", xsrfTokenValue);
//    HttpEntity<Void> refreshRequest = new HttpEntity<>(null, refreshHeaders);
//
//    // when
//    ResponseEntity<String> refreshResponse = restTemplate.postForEntity(
//        "/api/auth/refresh", refreshRequest, String.class
//    );
//
//    // then
//    assertThat(refreshResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
//
//    assertNotNull(refreshResponse.getBody());
//    assertNotEquals(loginResponse.getBody(), refreshResponse.getBody());
//
//    List<String> cookies = refreshResponse.getHeaders().get(HttpHeaders.SET_COOKIE);
//    assertThat(cookies).isNotNull();
//
//    Optional<String> refreshCookie = cookies.stream()
//        .filter(cookie -> cookie.startsWith("refresh_token="))
//        .findFirst();
//
//    assertThat(refreshCookie).isPresent();
//    assertNotEquals(refreshTokenCookie, refreshCookie);
//  }
//
//
//}
