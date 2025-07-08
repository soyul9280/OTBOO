package com.codeit.weatherwear.domain.feed.controller;

import com.codeit.weatherwear.domain.feed.dto.response.FeedDto;
import com.codeit.weatherwear.domain.feed.service.FeedLikeService;
import com.codeit.weatherwear.domain.security.customauthentication.CustomUserDetails;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feeds/{feedId}/like")
@RequiredArgsConstructor
public class FeedLikeController {

  private final FeedLikeService feedLikeService;

  @PostMapping
  public ResponseEntity<FeedDto> addFeedLike(
      @PathVariable UUID feedId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(feedLikeService.addFeedLike(feedId, userDetails.getUserId()));
  }

  @DeleteMapping
  public ResponseEntity<Void> deleteFeedLike(
      @PathVariable UUID feedId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    feedLikeService.deleteFeedLike(feedId, userDetails.getUserId());
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }


}
