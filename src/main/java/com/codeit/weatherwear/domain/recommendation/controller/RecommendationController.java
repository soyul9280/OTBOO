package com.codeit.weatherwear.domain.recommendation.controller;

import com.codeit.weatherwear.domain.recommendation.dto.RecommendationDto;
import com.codeit.weatherwear.domain.recommendation.service.RecommendationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
public class RecommendationController implements RecommendationApi{

  private final RecommendationService recommendationService;

  /**
   * 의상 추천 목록을 조회합니다.
   *
   * @param weatherId 날씨ID
   * @return 200 400
   */
  @Override
  @GetMapping
  public ResponseEntity<RecommendationDto> searchRecommendations(@RequestParam UUID weatherId) {
    RecommendationDto result = recommendationService.recommendClothes(weatherId);
    return ResponseEntity.ok(result);
  }
}
