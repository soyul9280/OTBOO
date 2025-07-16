package com.codeit.weatherwear.domain.follow;

import com.codeit.weatherwear.domain.follow.dto.FollowDto;
import com.codeit.weatherwear.domain.follow.dto.FollowSummaryDto;
import com.codeit.weatherwear.domain.follow.dto.request.FollowCreateRequest;
import com.codeit.weatherwear.domain.follow.dto.request.FollowerRequest;
import com.codeit.weatherwear.domain.follow.dto.request.FollowingRequest;
import com.codeit.weatherwear.domain.security.customauthentication.CustomUserDetails;
import com.codeit.weatherwear.global.response.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/follows")
public class FollowController {

  private final FollowService followService;

  @PostMapping
  public ResponseEntity<FollowDto> postFollow(
      @RequestBody @Valid FollowCreateRequest followCreateRequest
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(followService.create(followCreateRequest));
  }

  @GetMapping("/summary")
  public ResponseEntity<FollowSummaryDto> getSummary(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam UUID userId
  ) {
    return ResponseEntity.ok(followService.getSummary(userId, userDetails.getUserId()));
  }

  @GetMapping("/followings")
  public ResponseEntity<PageResponse<FollowDto>> getFollowings(
      @Valid @ModelAttribute FollowingRequest followingRequest
  ) {
    PageRequest pageable = PageRequest.of(0, followingRequest.limit());
    return ResponseEntity.ok(followService.getFollowings(followingRequest, pageable));
  }

  @GetMapping("/followers")
  public ResponseEntity<PageResponse<FollowDto>> getFollower(
      @Valid @ModelAttribute FollowerRequest followerRequest
  ) {
    PageRequest pageable = PageRequest.of(0, followerRequest.limit());
    return ResponseEntity
        .ok(followService.getFollowers(followerRequest, pageable));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    followService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
