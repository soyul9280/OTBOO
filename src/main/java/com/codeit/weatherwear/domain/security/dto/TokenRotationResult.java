package com.codeit.weatherwear.domain.security.dto;

public record TokenRotationResult(
    String accessToken,
    String refreshToken
) {

}
