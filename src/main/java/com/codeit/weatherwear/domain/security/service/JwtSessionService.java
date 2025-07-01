package com.codeit.weatherwear.domain.security.service;

import com.codeit.weatherwear.domain.security.config.properties.JwtProperties;
import com.codeit.weatherwear.domain.security.dto.TokenRotationResult;
import com.codeit.weatherwear.domain.security.entity.JwtSession;
import com.codeit.weatherwear.domain.security.exception.InvalidJwtException;
import com.codeit.weatherwear.domain.security.exception.JwtSessionNotFoundException;
import com.codeit.weatherwear.domain.security.repository.JwtSessionRepository;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtSessionService {

    private final JwtSessionRepository jwtSessionRepository;
    private final JwtProperties jwtProperties;
    private final Clock clock;
    private SecretKey signingKey;
    private final UserRepository userRepository;

    public enum TokenType {
        ACCESS, REFRESH
    }

    @Transactional
    public JwtSession createJwtSession(UUID userId) {
        Instant now = clock.instant();
        Instant accessTokenExpirationTime = now.plusSeconds(
            jwtProperties.getAccessToken().getValiditySeconds());
        Instant refreshTokenExpirationTime = now.plusSeconds(
            jwtProperties.getRefreshToken().getValiditySeconds());

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String accessToken = createTokenWithClaims(user, TokenType.ACCESS,
            accessTokenExpirationTime);
        String refreshToken = createTokenWithClaims(user, TokenType.REFRESH,
            refreshTokenExpirationTime);

        JwtSession jwtSession = jwtSessionRepository.save(
            new JwtSession(
                userId,
                accessToken,
                refreshToken,
                accessTokenExpirationTime
            )
        );
        return jwtSession;
    }

    // 토큰 생성
    private String createTokenWithClaims(User user, TokenType tokenType, Instant expirationTime) {
        Instant now = clock.instant();

        JwtBuilder builder = Jwts.builder()
            .header()
            .add("typ", "JWT")
            .and()
            .issuer(jwtProperties.getIssuer())
            .subject(user.getEmail())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expirationTime))
            .claim("type", tokenType.name())
            .claim("userId", user.getId().toString())
            .claim("name", user.getName())
            .claim("role", user.getRole().name())
            .claim("email", user.getEmail());

        return builder
            .signWith(getSigningKey(), SIG.HS256)
            .compact();
    }

    // 토큰 유효성 검증
    public boolean isValidToken(String token) {
        try {

            JwtParser parser = Jwts.parser()
                .verifyWith(getSigningKey())
                .build();
            parser.parseClaimsJws(token);

            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
        }
        return false;
    }

    // 로그인 상태 확인
    public boolean isSignedIn(String token) {
        return jwtSessionRepository.existsByAccessToken(token);
    }


    // 서명키 생성
    private SecretKey getSigningKey() {
        if (this.signingKey == null) {

            byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
            signingKey = Keys.hmacShaKeyFor(keyBytes);
        }
        return this.signingKey;
    }

    // 토큰에서 사용자 ID 추출
    public UUID extractUserId(String token) {
        try {
            JwtParser parser = Jwts.parser()
                .verifyWith(getSigningKey())
                .build();

            Claims claims = parser
                .parseSignedClaims(token)
                .getPayload();

            String userId = claims.get("userId", String.class);
            return UUID.fromString(userId);
        } catch (JwtException e) {
            log.error("Failed to extract user ID from token", e);
            throw new InvalidJwtException();
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format in token subject", e);
            throw new InvalidJwtException();
        }
    }

    // 리프레시 토큰으로 강제 로그아웃
    @Transactional
    public void invalidateToken(String refreshToken) {
        jwtSessionRepository.findByRefreshToken(refreshToken).ifPresentOrElse(
            jwtSession -> {
                // TODO: 블랙리스트 추가
                jwtSessionRepository.delete(jwtSession);
            },
            () -> log.info("No active JwtSession found for refreshToken: {}", refreshToken)
        );
    }

    // userId로 강제 로그아웃
    @Transactional
    public void invalidateToken(UUID userId) {
        jwtSessionRepository.findByUserId(userId).ifPresentOrElse(
            jwtSession -> {
                // TODO: 블랙리스트 추가
                jwtSessionRepository.delete(jwtSession);
            },
            () -> log.info("No active JwtSession found for userId: {}", userId)
        );
    }

    @Transactional
    // 리프레시 토큰으로 액세스, 리프레시 토큰 재발급
    public TokenRotationResult rotateToken(String refreshToken) {
        if (!isValidToken(refreshToken)) {
            throw new InvalidJwtException();
        }
        JwtSession jwtSession = jwtSessionRepository.findByRefreshToken(refreshToken)
            .orElseThrow(() -> new JwtSessionNotFoundException());

        User user = userRepository.findById(jwtSession.getUserId())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Instant now = clock.instant();
        Instant newAccessTokenExpirationTime = now.plusSeconds(
            jwtProperties.getAccessToken().getValiditySeconds());
        Instant newRefreshTokenExpirationTime = now.plusSeconds(
            jwtProperties.getRefreshToken().getValiditySeconds());
        String newAccessToken = createTokenWithClaims(user, TokenType.ACCESS,
            newAccessTokenExpirationTime);
        String newRefreshToken = createTokenWithClaims(user, TokenType.REFRESH,
            newRefreshTokenExpirationTime);

        jwtSession.update(newAccessToken, newRefreshToken, newAccessTokenExpirationTime);

        return new TokenRotationResult(newAccessToken, newRefreshToken);
    }

    // 리프레시 토큰으로 액세스 토큰 조회
    public String findAccessToken(String refreshToken) {
        JwtSession jwtSession = jwtSessionRepository.findByRefreshToken(refreshToken)
            .orElseThrow(() -> new JwtSessionNotFoundException());
        return jwtSession.getAccessToken();
    }

}
