package com.codeit.weatherwear.global.sse;

import com.codeit.weatherwear.domain.security.customauthentication.CustomUserDetails;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
public class SseController {

  private final SseService sseService;

  @GetMapping
  public SseEmitter connect(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(value = "LastEventId",required = false)UUID lastEventId
  ) {
    UUID userId = userDetails.getUserId();
    return sseService.connect(userId, lastEventId);
  }
}
