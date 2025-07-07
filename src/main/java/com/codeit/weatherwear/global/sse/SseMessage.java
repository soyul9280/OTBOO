package com.codeit.weatherwear.global.sse;

import com.codeit.weatherwear.domain.notification.NotificationDto;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter.DataWithMediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SseMessage {

  private static final String EVENT_NAME = "notifications";

  private UUID eventId;
  private Set<UUID> receiverIds;
  private NotificationDto eventData;

  public static SseMessage create(UUID receiverId, NotificationDto eventData) {
    return new SseMessage(
        UUID.randomUUID(),
        Set.of(receiverId),
        eventData
    );
  }

  public boolean isReceivable(UUID receiverId) {
    return receiverIds.contains(receiverId);
  }

  public Set<DataWithMediaType> toEvent() {
    return SseEmitter.event()
        .id(eventId.toString())
        .name(EVENT_NAME)
        .data(eventData)
        .build();
  }
}
