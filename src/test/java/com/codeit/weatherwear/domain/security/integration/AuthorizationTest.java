package com.codeit.weatherwear.domain.security.integration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.codeit.weatherwear.domain.clothes.entity.Cloth;
import com.codeit.weatherwear.domain.clothes.entity.ClothType;
import com.codeit.weatherwear.domain.clothes.repository.ClothRepository;
import com.codeit.weatherwear.domain.feed.repository.FeedRepository;
import com.codeit.weatherwear.domain.user.dto.response.UserDto;
import com.codeit.weatherwear.domain.user.entity.Role;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.exception.UserNotFoundException;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import com.codeit.weatherwear.domain.weather.repository.WeatherRepository;
import com.codeit.weatherwear.global.config.ContainerInitializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test-security")
@ContextConfiguration(initializers = ContainerInitializer.class)
@Slf4j
public class AuthorizationTest {

  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private TestRestTemplate restTemplate;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private ClothRepository clothRepository;
  @Autowired
  private FeedRepository feedRepository;
  @Autowired
  private WeatherRepository weatherRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;

  UUID userId;
  UUID adminUserId;

  private static class TestAuthSession {

    String refreshToken;
    String xsrfTokenCookie;
    String xsrfTokenValue;
    String accessToken;

    public TestAuthSession(String refreshToken, String xsrfTokenCookie, String xsrfTokenValue,
        String accessToken) {
      this.refreshToken = refreshToken;
      this.xsrfTokenCookie = xsrfTokenCookie;
      this.xsrfTokenValue = xsrfTokenValue;
      this.accessToken = accessToken;
    }
  }


  @BeforeEach
  void setUp() {
    // 일반 유저
    User user = userRepository.save(
        User.builder()
            .email("test@mail.com")
            .password(passwordEncoder.encode("password"))
            .name("testname")
            .role(Role.USER)
            .build()
    );
    // 관리자 유저
    User admin = userRepository.save(
        User.builder()
            .email("admin@mail.com")
            .password(passwordEncoder.encode("password"))
            .name("testadmin")
            .role(Role.ADMIN)
            .build()
    );
    userId = user.getId();
    adminUserId = admin.getId();
  }

  @AfterEach
  void tearDown() {
    userRepository.deleteAll();
    clothRepository.deleteAll();
  }

  private TestAuthSession authenticateWithCsrf(String email, String password) {
    /**
     * 로그인 요청
     */
    // given
    Map<String, String> loginBody = Map.of(
        "email", email,
        "password", password
    );
    HttpHeaders loginHeaders = new HttpHeaders();
    loginHeaders.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, String>> loginRequest = new HttpEntity<>(loginBody, loginHeaders);

    // when
    ResponseEntity<String> loginResponse = restTemplate.postForEntity(
        "/api/auth/sign-in", loginRequest, String.class
    );

    // then
    assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    // body에서 액세스 토큰 추출
    String accessToken = loginResponse.getBody().replace("\"", "");
    // cookie에서 리프레시 토큰 추출
    String refreshTokenCookie = loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE).stream()
        .filter(cookie -> cookie.startsWith("refresh_token="))
        .map(cookie -> cookie.split(";", 2)[0])
        .findFirst()
        .orElseThrow();

    /**
     * CSRF 요청
     */
    // given
    HttpHeaders csrfHeaders = new HttpHeaders();
    csrfHeaders.set("Cookie", refreshTokenCookie);
    HttpEntity<Void> csrfRequest = new HttpEntity<>(null, csrfHeaders);

    // when
    ResponseEntity<String> csrfResponse = restTemplate.exchange(
        "/api/auth/csrf-token", HttpMethod.GET, csrfRequest, String.class
    );

    // then
    assertThat(csrfResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    String xsrfTokenCookie = csrfResponse.getHeaders().get(HttpHeaders.SET_COOKIE).stream()
        .filter(cookie -> cookie.startsWith("XSRF-TOKEN="))
        .map(cookie -> cookie.split(";", 2)[0])
        .findFirst()
        .orElseThrow();

    String xsrfTokenValue = xsrfTokenCookie.split("=")[1];

    return new TestAuthSession(refreshTokenCookie, xsrfTokenCookie, xsrfTokenValue, accessToken);
  }

  /**
   * 인증 정보를 담은 HttpHeaders 생성
   */
  private HttpHeaders authenticatedHeaders(TestAuthSession session) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Cookie", session.refreshToken + "; " + session.xsrfTokenCookie);
    headers.set("X-XSRF-TOKEN", session.xsrfTokenValue);
    headers.setBearerAuth(session.accessToken);
    return headers;
  }

  @Test
  @DisplayName("ADMIN은 권한을 수정할 수 있다: hasRole('ADMIN') 테스트")
  void adminCanUpadteRole() {
    // 인증 정보 얻기
    TestAuthSession session = authenticateWithCsrf("admin@mail.com", "password");

    // given
    Map<String, String> updateBody = Map.of("role", "ADMIN");
    HttpEntity<Map<String, String>> request = new HttpEntity<>(updateBody,
        authenticatedHeaders(session));

    // when
    ResponseEntity<UserDto> response = restTemplate.exchange(
        "/api/users/" + userId + "/role",
        HttpMethod.PATCH,
        request,
        UserDto.class
    );

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getRole()).isEqualTo(Role.ADMIN);
  }

  @Test
  @DisplayName("USER은 권한을 수정할 수 없다: hasRole('ADMIN') 테스트")
  void userCannotUpadteRole() {
    // 인증 정보 얻기
    TestAuthSession session = authenticateWithCsrf("test@mail.com", "password");

    // given
    Map<String, String> updateBody = Map.of("role", "ADMIN");
    HttpEntity<Map<String, String>> request = new HttpEntity<>(updateBody,
        authenticatedHeaders(session));

    // when
    ResponseEntity<UserDto> response = restTemplate.exchange(
        "/api/users/" + userId + "/role",
        HttpMethod.PATCH,
        request,
        UserDto.class
    );

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  @DisplayName("자신의 비밀번호를 변경할 수 있다: #userId == principal.userId 테스트")
  void successUpdatePassword() {
    // 인증 정보 얻기
    TestAuthSession session = authenticateWithCsrf("test@mail.com", "password");

    // given
    Map<String, String> updateBody = Map.of("password", "newPassword");
    HttpEntity<Map<String, String>> request = new HttpEntity<>(updateBody,
        authenticatedHeaders(session));

    // when
    ResponseEntity<Void> response = restTemplate.exchange(
        "/api/users/" + userId + "/password",
        HttpMethod.PATCH,
        request,
        Void.class
    );

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  @DisplayName("다른 사람의 비밀번호를 변경할 수 없다: #userId == principal.userId 테스트")
  void FailUpdatePassword() {
    // 인증 정보 얻기
    TestAuthSession session = authenticateWithCsrf("test@mail.com", "password");

    // given
    Map<String, String> updateBody = Map.of("password", "newPassword");
    HttpEntity<Map<String, String>> request = new HttpEntity<>(updateBody,
        authenticatedHeaders(session));

    // when
    ResponseEntity<Void> response = restTemplate.exchange(
        "/api/users/" + adminUserId + "/password",
        HttpMethod.PATCH,
        request,
        Void.class
    );

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  @DisplayName("자신의 옷을 삭제할 수 있다: @authorizationEvaluator.isClothOwner(authentication.principal.userId, #clothesId) 테스트")
  void successDeleteClothes() {
    // given
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException());
    Cloth cloth = clothRepository.save(
        Cloth.builder()
            .name("name")
            .clothType(ClothType.DRESS)
            .user(user)
            .build()
    );
    UUID clothId = cloth.getId();

    // 인증 정보 얻기
    TestAuthSession session = authenticateWithCsrf("test@mail.com", "password");
    HttpEntity<Map<String, String>> request = new HttpEntity<>(null,
        authenticatedHeaders(session));
    // when
    ResponseEntity<Void> response = restTemplate.exchange(
        "/api/clothes/" + clothId,
        HttpMethod.DELETE,
        request,
        Void.class
    );

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

  @Test
  @DisplayName("관리자나 자신의 옷이 아니면 삭제할 수 없다: @authorizationEvaluator.isClothOwner(authentication.principal.userId, #clothesId) 테스트")
  void failDeleteClothes() {
    // given
    User admin = userRepository.findById(adminUserId)
        .orElseThrow(() -> new UserNotFoundException());
    Cloth cloth = clothRepository.save(
        Cloth.builder()
            .name("name")
            .clothType(ClothType.DRESS)
            .user(admin)
            .build()
    );
    UUID clothId = cloth.getId();

    // 인증 정보 얻기
    TestAuthSession session = authenticateWithCsrf("test@mail.com", "password");
    HttpEntity<Map<String, String>> request = new HttpEntity<>(null,
        authenticatedHeaders(session));
    // when
    ResponseEntity<Void> response = restTemplate.exchange(
        "/api/clothes/" + clothId,
        HttpMethod.DELETE,
        request,
        Void.class
    );

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    clothRepository.delete(cloth);
  }

}
