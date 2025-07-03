package com.codeit.weatherwear.domain.recommendation.service;

import com.codeit.weatherwear.domain.recommendation.dto.RecommendationDto;
import java.util.UUID;

public interface RecommendationService {
  RecommendationDto recommendClothes(UUID weatherId);
}
