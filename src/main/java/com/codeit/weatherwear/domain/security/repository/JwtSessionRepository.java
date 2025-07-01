package com.codeit.weatherwear.domain.security.repository;

import com.codeit.weatherwear.domain.security.entity.JwtSession;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JwtSessionRepository extends JpaRepository<JwtSession, UUID> {

    Optional<JwtSession> findByRefreshToken(String refreshToken);

    Optional<JwtSession> findByUserId(UUID userId);

    boolean existsByAccessToken(String token);

    boolean existsByRefreshToken(String token);
}
