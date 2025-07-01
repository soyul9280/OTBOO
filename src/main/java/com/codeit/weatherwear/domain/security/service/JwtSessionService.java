package com.codeit.weatherwear.domain.security.service;

import com.codeit.weatherwear.domain.security.config.properties.JwtProperties;
import com.codeit.weatherwear.domain.security.entity.JwtSession;
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
        Instant now = Instant.now();
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

    // 토큰 유효성 인증
    public boolean validateToken(String token) {
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

    // 서명키 생성
    private SecretKey getSigningKey() {
        if (this.signingKey == null) {

            byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
            return Keys.hmacShaKeyFor(keyBytes);
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
            throw new IllegalArgumentException("Invalid JWT token", e);
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format in token subject", e);
            throw new IllegalArgumentException("Invalid user ID format in token", e);
        }
    }

    @Transactional
    public void invalidateToken(String refreshToken) {
        JwtSession jwtSession = jwtSessionRepository.findByRefreshToken(refreshToken)
            // TODO: 커스텀 예외로 변경
            .orElseThrow(() -> new IllegalArgumentException());

        // TODO:토큰을 블랙리스트에 추가

        jwtSessionRepository.delete(jwtSession);
    }

}
