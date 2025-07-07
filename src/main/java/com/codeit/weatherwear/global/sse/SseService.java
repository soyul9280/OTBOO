package com.codeit.weatherwear.global.sse;

import com.codeit.weatherwear.domain.notification.NotificationDto;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter.DataWithMediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

  private final SseEmitterRepository sseEmitterRepository;
  private final SseMessageRepository sseMessageRepository;

  @Value("${sse.timeout}")
  private long timeout;

  public SseEmitter connect(UUID receiverId, UUID lastEventId) {
    SseEmitter sseEmitter = new SseEmitter(timeout);

    sseEmitter.onCompletion(() -> {
      log.debug("SseEmitter onCompletion");
      sseEmitterRepository.delete(receiverId, sseEmitter);
    });
    sseEmitter.onTimeout(() -> {
      log.debug("SseEmitter onTimeout");
      sseEmitterRepository.delete(receiverId, sseEmitter);
    });
    sseEmitter.onError((error) -> {
      log.debug("SseEmitter onError");
      sseEmitterRepository.delete(receiverId, sseEmitter);
    });

    sseEmitterRepository.save(receiverId, sseEmitter);

    if (lastEventId != null) {
      sseMessageRepository.findAllByEventIdAfterAndReceiverId(lastEventId, receiverId)
          .forEach(sseMessage -> {
            try {
              sseEmitter.send(sseMessage.toEvent());
            } catch (IOException e) {
              log.error("SSE 전송 실패. receiverId={}, lastEventId={}", receiverId, lastEventId, e);
              sseEmitter.completeWithError(e);
              sseEmitterRepository.delete(receiverId, sseEmitter);
            }
          });
    }

    return sseEmitter;
  }

  public void send(UUID receiverId, NotificationDto data) {
    SseMessage message = sseMessageRepository.save(SseMessage.create(receiverId, data));
    sseEmitterRepository.findByReceiverId(receiverId)
        .forEach(sseEmitter -> {
          try {
            sseEmitter.send(message.toEvent());
          } catch (IOException e) {
            log.error("SSE 전송 실패. receiverId={}, notificationId={}", receiverId, data.id(), e);
            sseEmitter.completeWithError(e);
            sseEmitterRepository.delete(receiverId, sseEmitter);
          }
        });
  }

  @Scheduled(cron = "0 */30 * * * *")
  public void clean() {
    Set<DataWithMediaType> ping = SseEmitter.event()
        .name("ping")
        .build();
    sseEmitterRepository.findAll()
        .forEach(sseEmitter -> {
          try {
            sseEmitter.send(ping);
          } catch (IOException e) {
            log.error(e.getMessage(), e);
            sseEmitter.completeWithError(e);
          }
        });
  }
}
