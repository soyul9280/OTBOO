package com.codeit.weatherwear.domain.feed.controller.api;

import com.codeit.weatherwear.domain.feed.dto.request.FeedCommentCreateRequest;
import com.codeit.weatherwear.domain.feed.dto.request.FeedCommentGetParamRequest;
import com.codeit.weatherwear.domain.feed.dto.response.FeedCommentDto;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "피드 관리", description = "피드 관련 API")
@RequestMapping("api/feeds/{feedId}/comments")
public interface FeedCommentApi {

  @Operation(summary = "피드 댓글 등록", description = "피드 댓글 등록 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "피드 댓글 등록 성공",
          content = @Content(schema = @Schema(implementation = FeedCommentDto.class))),
      @ApiResponse(
          responseCode = "400",
          description = "피드 댓글 등록 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @PostMapping
  ResponseEntity<FeedCommentDto> createFeedComment(
      @PathVariable UUID feedId,
      @RequestBody FeedCommentCreateRequest request
  );

  @Operation(summary = "피드 댓글 조회", description = "피드 댓글 조회 API")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "피드 댓글 조회 성공"
      ),
      @ApiResponse(
          responseCode = "400",
          description = "피드 댓글 조회 실패",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @GetMapping
  ResponseEntity<PageResponse<FeedCommentDto>> getFeedComment(
      @PathVariable UUID feedId,
      @ModelAttribute @Valid FeedCommentGetParamRequest queryRequest
  );
}
