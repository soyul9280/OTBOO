package com.codeit.weatherwear.domain.feed.controller.api;

import com.codeit.weatherwear.domain.feed.dto.response.FeedDto;
import com.codeit.weatherwear.domain.security.customauthentication.CustomUserDetails;
import com.codeit.weatherwear.global.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "피드 관리", description = "피드 관련 API")
@RequestMapping("/api/feeds/{feedId}/like")
public interface FeedLikeApi {

  @Operation(summary = "피드 좋아요", description = "피드 좋아요 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "피드 좋아요 성공",
          content = @Content(schema = @Schema(implementation = FeedDto.class))),
      @ApiResponse(
          responseCode = "400",
          description = "피드 좋아요 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @PostMapping
  ResponseEntity<FeedDto> addFeedLike(
      @PathVariable UUID feedId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  );

  @Operation(summary = "피드 좋아요 취소", description = "피드 좋아요 취소 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "204",
          description = "피드 좋아요 취소 성공"),
      @ApiResponse(
          responseCode = "400",
          description = "피드 좋아요 취소 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @DeleteMapping
  ResponseEntity<Void> deleteFeedLike(
      @PathVariable UUID feedId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  );
}
