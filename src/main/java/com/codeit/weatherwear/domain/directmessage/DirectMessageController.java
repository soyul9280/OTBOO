package com.codeit.weatherwear.domain.directmessage;

import com.codeit.weatherwear.domain.security.customauthentication.CustomUserDetails;
import com.codeit.weatherwear.global.response.PageResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/direct-messages")
public class DirectMessageController {

  private final DirectMessageService directMessageService;

  @GetMapping
  public ResponseEntity<PageResponse<DirectMessageDto>> getDirectMessages(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam UUID userId,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) UUID idAfter,
      @RequestParam int limit
  ) {
    PageRequest pageRequest = PageRequest.of(0, limit);
    return ResponseEntity.ok(directMessageService
        .findDirectMessages(userDetails.getUserId(), userId, cursor, idAfter, pageRequest));
  }



}
