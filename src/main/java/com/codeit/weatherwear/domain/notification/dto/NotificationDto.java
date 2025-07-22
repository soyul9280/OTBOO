package com.codeit.weatherwear.domain.notification.dto;

import com.codeit.weatherwear.domain.notification.Notification;
import com.codeit.weatherwear.domain.notification.Notification.Level;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(name = "알림 정보")
public record NotificationDto(
    UUID id,
    Instant createdAt,
    UUID receiverId,
    String title,
    String content,
    Level level
) {
  public static NotificationDto from(Notification notification) {
    return new NotificationDto(
        notification.getId(),
        notification.getCreatedAt(),
        notification.getReceiverId(),
        notification.getTitle(),
        notification.getContent(),
        notification.getLevel()
    );
  }
}
