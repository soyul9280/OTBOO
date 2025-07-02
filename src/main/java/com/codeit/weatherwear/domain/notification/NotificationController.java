package com.codeit.weatherwear.domain.notification;

import com.codeit.weatherwear.global.response.PageResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<PageResponse<NotificationDto>> getNotifications(
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) UUID idAfter,
      @RequestParam int limit
  ) {
    //인증 기능이 완료되면 유저의 id를 가져오는 것으로 변경
    UUID tempId = UUID.randomUUID();
    PageRequest pageable = PageRequest.of(0, limit);
    return ResponseEntity.ok(notificationService.findNotification(tempId, cursor, idAfter, pageable));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteNotification(@PathVariable UUID id) {
    notificationService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
