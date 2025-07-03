package com.codeit.weatherwear.domain.recommendation.service;

import com.codeit.weatherwear.domain.recommendation.dto.RecommendationDto;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {

  /**
   * 의상 추천
   *
   * @param weatherId 날씨 ID
   * @return 추천 DTO
   */
  @Override
  public RecommendationDto recommendClothes(UUID weatherId) {
    return null;
  }
}
