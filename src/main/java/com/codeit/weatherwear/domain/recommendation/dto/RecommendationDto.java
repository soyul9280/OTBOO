package com.codeit.weatherwear.domain.recommendation.dto;

import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import java.util.List;
import java.util.UUID;

public record RecommendationDto(
    UUID weatherId,
    UUID userId,
    List<ClothesDto> clothes
) {

}
