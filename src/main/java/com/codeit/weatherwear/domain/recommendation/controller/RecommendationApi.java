package com.codeit.weatherwear.domain.recommendation.controller;

import com.codeit.weatherwear.domain.recommendation.dto.RecommendationDto;
import com.codeit.weatherwear.global.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "추천 관리", description = "추천 관련 API")
@RequestMapping("/api/recommendations")
public interface RecommendationApi {

  @Operation(summary = "추천 조회", description = "추천 조회 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "추천 조회 성공",
          content = @Content(schema = @Schema(implementation = RecommendationDto.class))),
      @ApiResponse(
          responseCode = "400",
          description = "추천 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @GetMapping
  ResponseEntity<RecommendationDto> searchRecommendations(
      @RequestParam(value = "weatherId") UUID weatherId);


}
