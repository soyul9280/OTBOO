package com.codeit.weatherwear.domain.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.weatherwear.domain.security.JwtBlacklist;
import com.codeit.weatherwear.domain.security.config.properties.JwtProperties;
import com.codeit.weatherwear.domain.security.dto.TokenRotationResult;
import com.codeit.weatherwear.domain.security.entity.JwtSession;
import com.codeit.weatherwear.domain.security.repository.JwtSessionRepository;
import com.codeit.weatherwear.domain.user.entity.Role;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JwtSessionServiceTest {

  private JwtSessionService jwtSessionService;
  @Mock
  private JwtSessionRepository jwtSessionRepository;
  @Mock
  private JwtBlacklist jwtBlacklist;
  @Mock
  private UserRepository userRepository;
  private JwtProperties jwtProperties;

  private final UUID userId = UUID.randomUUID();

  private User user;

  @BeforeEach
  void setUp() {
    user = User.builder()
        .id(userId)
        .name("testname")
        .password("testpassword")
        .email("test@test.com")
        .role(Role.USER)
        .build();

    // jwtProperties는 @ConfigurationProperties로 final 기반이라 Mockito로 mock하기 어려워 직접 생성
    JwtProperties.TokenConfig accessTokenConfig = new JwtProperties.TokenConfig(3600L);
    JwtProperties.TokenConfig refreshTokenConfig = new JwtProperties.TokenConfig(7200L);
    jwtProperties = new JwtProperties(
        "test-issuer",
        "my-test-secret-key-my-test-secret-key",
        accessTokenConfig,
        refreshTokenConfig
    );

    jwtSessionService = new JwtSessionService(
        jwtSessionRepository,
        jwtProperties,
        jwtBlacklist,
        userRepository
    );
  }

  @Test
  void JwtSession_생성_성공() {
    // given
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(jwtSessionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // when
    Instant before = Instant.now();
    JwtSession session = jwtSessionService.createJwtSession(userId);

    // then
    assertEquals(userId, session.getUserId());
    assertNotNull(session.getAccessToken());
    assertNotNull(session.getRefreshToken());
    // 토큰 만료 시간 체크 (2s 오차 허용)
    long actualValidity = session.getExpirationTime().getEpochSecond() - before.getEpochSecond();
    long expectedValidity = jwtProperties.getAccessToken().getValiditySeconds();
    assertTrue(actualValidity <= expectedValidity && actualValidity >= expectedValidity - 2);
  }

  @Test
  @DisplayName("유효한 토큰 검증 성공")
  void isValidToken_returnsTrue() {
    // given
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(jwtSessionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(jwtBlacklist.existsInBlacklist(anyString())).thenReturn(false);  // 블랙리스트에 없다고 가정

    JwtSession session = jwtSessionService.createJwtSession(userId);
    String accessToken = session.getAccessToken();

    // when
    boolean result = jwtSessionService.isValidToken(accessToken);
    // then
    assertTrue(result);
  }

  @Test
  @DisplayName("만료된 토큰에 대해 유효하지 않음 검증")
  void isValidToken_returnsFalse_forExpiredToken() {
    // given
    // 만료된 시간으로 토큰 생성
    Instant issuedAt = Instant.now().minusSeconds(3600); // 1시간 전
    Instant expiredAt = Instant.now().minusSeconds(1800); // 30분 전에 만료됨

    SecretKey key = Keys.hmacShaKeyFor(
        jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));

    String expiredToken = Jwts.builder()
        .issuer(jwtProperties.getIssuer())
        .issuedAt(Date.from(issuedAt))
        .expiration(Date.from(expiredAt))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();

    // when
    boolean result = jwtSessionService.isValidToken(expiredToken);
    // then
    assertFalse(result);
  }

  @Test
  @DisplayName("리프레시 토큰으로 토큰 무효화")
  void invalidateToken_byRefreshToken_deletesSessionAndBlacklistsToken() {
    String refreshToken = "refresh-token";
    String accessToken = "access-token";
    Instant expirationTime = Instant.now().plusSeconds(3600);

    JwtSession session = new JwtSession(userId, accessToken, refreshToken, expirationTime);
    when(jwtSessionRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.of(session));

    jwtSessionService.invalidateToken(refreshToken);

    verify(jwtBlacklist).addBlacklist(accessToken, expirationTime);
    verify(jwtSessionRepository).delete(session);
  }

  @Test
  @DisplayName("userId로 토큰 무효화")
  void invalidateToken_byUserId_deletesSessionAndBlacklistsToken() {
    String refreshToken = "refresh-token";
    String accessToken = "access-token";
    Instant expirationTime = Instant.now().plusSeconds(3600);

    JwtSession session = new JwtSession(userId, accessToken, refreshToken, expirationTime);
    when(jwtSessionRepository.findByUserId(userId)).thenReturn(Optional.of(session));

    jwtSessionService.invalidateToken(userId);

    verify(jwtBlacklist).addBlacklist(accessToken, expirationTime);
    verify(jwtSessionRepository).delete(session);
  }

  @Test
  void 토큰_재발급_성공() {
    // given
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(jwtBlacklist.existsInBlacklist(anyString())).thenReturn(false);

    // 실제 유효한 refresh 토큰 생성
    Instant now = Instant.now();
    Instant expiration = now.plusSeconds(jwtProperties.getRefreshToken().getValiditySeconds());
    SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));

    String refreshToken = Jwts.builder()
        .issuer(jwtProperties.getIssuer())
        .subject(user.getEmail())
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiration))
        .claim("type", "REFRESH")
        .claim("userId", user.getId().toString())
        .claim("name", user.getName())
        .claim("role", user.getRole().name())
        .claim("email", user.getEmail())
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();

    JwtSession session = new JwtSession(userId, "oldAccess", refreshToken, expiration);
    when(jwtSessionRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.of(session));

    // when
    TokenRotationResult result = jwtSessionService.rotateToken(refreshToken);

    // then
    assertNotNull(result);
    assertNotNull(result.accessToken());
    assertNotNull(result.refreshToken());
    verify(jwtBlacklist).addBlacklist(eq("oldAccess"), any());
  }

  @Test
  void 토큰에서_유저_정보_추출_성공() {
    // given
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(jwtSessionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    JwtSession session = jwtSessionService.createJwtSession(userId);

    // when
    UUID extracted = jwtSessionService.extractUserId(session.getAccessToken());

    // then
    assertEquals(userId, extracted);
  }

  @Test
  void 리프레시토큰으로_액세스토큰_조회_성공() {
    // given
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(jwtSessionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    JwtSession session = jwtSessionService.createJwtSession(userId);
    String refreshToken = session.getRefreshToken();
    when(jwtSessionRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.of(session));

    // when
    String accessToken = jwtSessionService.findAccessToken(refreshToken);

    // then
    assertEquals(accessToken, session.getAccessToken());
  }
}