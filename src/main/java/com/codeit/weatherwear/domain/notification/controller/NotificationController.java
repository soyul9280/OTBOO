package com.codeit.weatherwear.domain.notification.controller;

import com.codeit.weatherwear.domain.notification.NotificationService;
import com.codeit.weatherwear.domain.notification.controller.api.NotificationApi;
import com.codeit.weatherwear.domain.notification.dto.NotificationDto;
import com.codeit.weatherwear.domain.notification.dto.request.NotificationSearchRequest;
import com.codeit.weatherwear.domain.security.customauthentication.CustomUserDetails;
import com.codeit.weatherwear.global.response.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController implements NotificationApi {

  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<PageResponse<NotificationDto>> getNotifications(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @ModelAttribute @Valid NotificationSearchRequest notificationSearchRequest
  ) {
    PageRequest pageable = PageRequest.of(0, notificationSearchRequest.limit());
    return ResponseEntity
        .ok(notificationService.findNotification(userDetails.getUserId(), notificationSearchRequest, pageable));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteNotification(@PathVariable UUID id) {
    notificationService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
