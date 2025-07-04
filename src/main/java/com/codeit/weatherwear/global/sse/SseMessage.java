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

  private static final String eventName = "notifications";

  private UUID eventId;
  private Set<UUID> receiverIds;
  private boolean isBroadcast;
  private NotificationDto eventData;

  public static SseMessage createBroadcast(UUID receiverId, NotificationDto eventData) {
    return new SseMessage(
        UUID.randomUUID(),
        Set.of(receiverId),
        false,
        eventData
    );
  }

  public static SseMessage createBroadcast(Set<UUID> receiverIds, NotificationDto eventData) {
    return new SseMessage(
        UUID.randomUUID(),
        receiverIds,
        false,
        eventData
    );
  }

  public static SseMessage createBroadcast(NotificationDto eventData) {
    return new SseMessage(
        UUID.randomUUID(),
        Set.of(),
        true,
        eventData
    );
  }

  public boolean isReceivable(UUID receiverId) {
    return isBroadcast || receiverIds.contains(receiverId);
  }

  public Set<DataWithMediaType> toEvent() {
    return SseEmitter.event()
        .id(eventId.toString())
        .name(eventName)
        .data(eventData)
        .build();
  }
}
