package com.codeit.weatherwear.domain.feed.controller;

import com.codeit.weatherwear.domain.feed.controller.api.FeedCommentApi;
import com.codeit.weatherwear.domain.feed.dto.request.FeedCommentCreateRequest;
import com.codeit.weatherwear.domain.feed.dto.request.FeedCommentGetParamRequest;
import com.codeit.weatherwear.domain.feed.dto.response.FeedCommentDto;
import com.codeit.weatherwear.domain.feed.service.FeedCommentService;
import com.codeit.weatherwear.global.response.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/feeds/{feedId}/comments")
@RequiredArgsConstructor
public class FeedCommentController implements FeedCommentApi {

  private final FeedCommentService feedCommentService;

  @PostMapping
  public ResponseEntity<FeedCommentDto> createFeedComment(
      @PathVariable UUID feedId,
      @RequestBody FeedCommentCreateRequest request
  ) {
    return ResponseEntity.ok(feedCommentService.createFeedComment(feedId, request));
  }

  @GetMapping
  public ResponseEntity<PageResponse<FeedCommentDto>> getFeedComment(
      @PathVariable UUID feedId,
      @ModelAttribute @Valid FeedCommentGetParamRequest queryRequest
  ) {
    return ResponseEntity.ok(feedCommentService.getFeedComments(feedId, queryRequest));
  }

}
