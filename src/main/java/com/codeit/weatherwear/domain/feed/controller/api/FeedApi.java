package com.codeit.weatherwear.domain.feed.controller.api;

import com.codeit.weatherwear.domain.feed.dto.request.FeedCreateRequest;
import com.codeit.weatherwear.domain.feed.dto.request.FeedGetParamRequest;
import com.codeit.weatherwear.domain.feed.dto.request.FeedUpdateRequest;
import com.codeit.weatherwear.domain.feed.dto.response.FeedDto;
import com.codeit.weatherwear.domain.security.customauthentication.CustomUserDetails;
import com.codeit.weatherwear.global.response.ErrorResponse;
import com.codeit.weatherwear.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "피드 관리", description = "피드 관련 API")
@RequestMapping("/api/feeds")
public interface FeedApi {

  @Operation(summary = "피드 목록 조회", description = "피드 목록 조회 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "피드 목록 조회 성공",
          content = @Content(schema = @Schema(implementation = PageResponse.class))),
      @ApiResponse(
          responseCode = "400",
          description = "피드 목록 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @GetMapping
  ResponseEntity<PageResponse<FeedDto>> getFeedList(
      @ModelAttribute @Valid FeedGetParamRequest paramRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails
  );

  @Operation(summary = "피드 등록", description = "피드 등록 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "201",
          description = "피드 등록 성공",
          content = @Content(schema = @Schema(implementation = FeedDto.class))),
      @ApiResponse(
          responseCode = "400",
          description = "피드 등록 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @PostMapping
  ResponseEntity<FeedDto> createFeed(@RequestBody FeedCreateRequest feedCreateRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails);

  @Operation(summary = "피드 수정", description = "피드 수정 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "피드 수정 성공",
          content = @Content(schema = @Schema(implementation = FeedDto.class))),
      @ApiResponse(
          responseCode = "400",
          description = "피드 수정 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @PreAuthorize("@authorizationEvaluator.isFeedAuthor(#feedId, authentication.principal.userId)")
  @PatchMapping("/{feedId}")
  ResponseEntity<FeedDto> updateFeed(
      @PathVariable UUID feedId,
      @RequestBody FeedUpdateRequest feedUpdateRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails);

  @Operation(summary = "피드 삭제", description = "피드 삭제 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "204",
          description = "피드 삭제 성공"),
      @ApiResponse(
          responseCode = "400",
          description = "피드 삭제 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @PreAuthorize("hasRole('ADMIN') or @authorizationEvaluator.isFeedAuthor(#feedId, authentication.principal.userId)")
  @DeleteMapping("/{feedId}")
  ResponseEntity<FeedDto> deleteFeed(@PathVariable UUID feedId,
      @AuthenticationPrincipal CustomUserDetails userDetails);

}
