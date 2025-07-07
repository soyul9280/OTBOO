package com.codeit.weatherwear.domain.event;

import com.codeit.weatherwear.domain.event.dto.MultipleNotificationCreatedEvent;
import com.codeit.weatherwear.domain.event.dto.NotificationCreatedEvent;
import com.codeit.weatherwear.domain.notification.NotificationDto;
import com.codeit.weatherwear.global.sse.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseEventListener {

  private final SseService sseService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(NotificationCreatedEvent event) {
    NotificationDto notificationDto = event.notificationDto();
    sseService.send(notificationDto.receiverId(), notificationDto);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(MultipleNotificationCreatedEvent event) {
    event.notificationDtos()
        .forEach(notificationDto ->
            sseService.send(notificationDto.receiverId(), notificationDto)
        );
  }
}
