package com.codeit.weatherwear.global.event.listener;

import com.codeit.weatherwear.domain.notification.dto.NotificationDto;
import com.codeit.weatherwear.global.event.dto.MultipleNotificationCreatedEvent;
import com.codeit.weatherwear.global.event.dto.NotificationCreatedEvent;
import com.codeit.weatherwear.global.event.exception.KafkaMessageConvertException;
import com.codeit.weatherwear.global.properties.KafkaTopics;
import com.codeit.weatherwear.global.sse.SseMessage;
import com.codeit.weatherwear.global.sse.SseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseEventListener {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final SseService sseService;
  private final ObjectMapper objectMapper;
  private final KafkaTopics kafkaTopics;

  @Async("eventExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(NotificationCreatedEvent event) {
    NotificationDto notificationDto = event.notificationDto();
    SseMessage sseMessage = SseMessage.create(notificationDto.receiverId(), notificationDto);
    sendToKafka(sseMessage);
  }

  @Async("eventExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(MultipleNotificationCreatedEvent event) {
    event.notificationDtos().stream()
        .map(notificationDto -> SseMessage.create(notificationDto.receiverId(), notificationDto))
        .forEach(this::sendToKafka);
  }

  @Async("eventExecutor")
  @KafkaListener(
      topics = "${spring.kafka.topics.sse-sent}",
      groupId = "#{T(java.util.UUID).randomUUID().toString()}"
  )
  public void handleSseSendEvent(String kafkaEvent) {
    SseMessage sseMessage = null;
    try {
      sseMessage = objectMapper.readValue(kafkaEvent, SseMessage.class);
    } catch (JsonProcessingException e) {
      throw KafkaMessageConvertException.withEvent(kafkaEvent);
    }
    sseService.send(sseMessage);
  }

  private void sendToKafka(SseMessage sseMessage) {
    String topic = kafkaTopics.sseSent();
    try {
      String payload = objectMapper.writeValueAsString(sseMessage);
      CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, payload);
      future.whenComplete((result, exception) -> {
        if (exception == null) {
          log.info("Produced event to topic {}: value = {}",
              result.getRecordMetadata().topic(), payload);
        } else {
          log.error("fail to send to kafka. topic={}, payload={}", topic, payload, exception);
        }
      });
    } catch (JsonProcessingException e) {
      log.error("fail to serialize {} to json. event={}",
          sseMessage.getClass().getSimpleName(), sseMessage, e);
      throw KafkaMessageConvertException.withEvent(sseMessage.toString());
    }
  }
}
