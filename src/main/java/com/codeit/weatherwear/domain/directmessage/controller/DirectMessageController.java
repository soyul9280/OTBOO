package com.codeit.weatherwear.domain.directmessage.controller;

import com.codeit.weatherwear.domain.directmessage.DirectMessageService;
import com.codeit.weatherwear.domain.directmessage.controller.api.DirectMessageApi;
import com.codeit.weatherwear.domain.directmessage.dto.DirectMessageDto;
import com.codeit.weatherwear.domain.directmessage.dto.request.DirectMessageSearchRequest;
import com.codeit.weatherwear.domain.security.customauthentication.CustomUserDetails;
import com.codeit.weatherwear.global.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/direct-messages")
public class DirectMessageController implements DirectMessageApi {

  private final DirectMessageService directMessageService;

  @GetMapping
  public ResponseEntity<PageResponse<DirectMessageDto>> getDirectMessages(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @ModelAttribute @Valid DirectMessageSearchRequest directMessageSearchRequest
  ) {
    PageRequest pageRequest = PageRequest.of(0, directMessageSearchRequest.limit());
    return ResponseEntity.ok(directMessageService
        .findDirectMessages(userDetails.getUserId(), directMessageSearchRequest, pageRequest));
  }
}
