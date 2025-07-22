package com.codeit.weatherwear.domain.follow.controller.api;

import com.codeit.weatherwear.domain.follow.dto.FollowDto;
import com.codeit.weatherwear.domain.follow.dto.FollowSummaryDto;
import com.codeit.weatherwear.domain.follow.dto.request.FollowCreateRequest;
import com.codeit.weatherwear.domain.follow.dto.request.FollowerSearchRequest;
import com.codeit.weatherwear.domain.follow.dto.request.FollowingSearchRequest;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "팔로우 관리", description = "팔로우 관련 API")
@RequestMapping("/api/follows")
public interface FollowApi {

  @Operation(summary = "팔로우 생성", description = "팔로우 생성 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "201",
          description = "팔로우 생성 성공"
      ),
      @ApiResponse(
          responseCode = "400",
          description = "팔로우 생성 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @PostMapping
  ResponseEntity<FollowDto> postFollow(
      @RequestBody @Valid FollowCreateRequest followCreateRequest
  );

  @Operation(summary = "팔로우 요약 정보 조회", description = "팔로우 요약 정보 조회 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "팔로우 요약 정보 조회 성공"
      ),
      @ApiResponse(
          responseCode = "400",
          description = "팔로우 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @GetMapping("/summary")
  ResponseEntity<FollowSummaryDto> getSummary(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam UUID userId
  );

  @Operation(summary = "팔로잉 목록 조회", description = "팔로우 목록 조회 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "팔로우 목록 조회 성공"
      ),
      @ApiResponse(
          responseCode = "400",
          description = "팔로우 목록 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @GetMapping("/followings")
  ResponseEntity<PageResponse<FollowDto>> getFollowings(
      @ModelAttribute @Valid FollowingSearchRequest followingSearchRequest
  );

  @Operation(summary = "팔로워 목록 조회", description = "팔로워 목록 조회 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "팔로워 목록 조회 성공"
      ),
      @ApiResponse(
          responseCode = "400",
          description = "팔로워 목록 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @GetMapping("/followers")
  ResponseEntity<PageResponse<FollowDto>> getFollower(
      @ModelAttribute @Valid FollowerSearchRequest followerSearchRequest
  );

  @Operation(summary = "팔로우 취소", description = "팔로우 취소 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "204",
          description = "팔로우 취소 성공"
      )
  })
  @DeleteMapping("/{id}")
  ResponseEntity<Void> delete(@PathVariable UUID id);
}
