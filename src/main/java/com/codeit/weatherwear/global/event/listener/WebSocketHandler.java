package com.codeit.weatherwear.global.event.listener;

import com.codeit.weatherwear.domain.directmessage.dto.DirectMessageDto;
import com.codeit.weatherwear.global.event.dto.DirectMessageReceivedEvent;
import com.codeit.weatherwear.global.event.exception.KafkaMessageConvertException;
import com.codeit.weatherwear.global.properties.KafkaTopics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler {

  private final SimpMessagingTemplate messagingTemplate;
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final KafkaTopics kafkaTopics;
  private final ObjectMapper objectMapper;

  @Async("eventExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(DirectMessageReceivedEvent event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      kafkaTemplate.send(kafkaTopics.directMessageSent(), payload)
          .whenComplete((result, exception) -> {
            if (exception == null) {
              log.info("Produced event to topic {}: value = {}", result.getRecordMetadata().topic(), payload);
            } else {
              log.error("fail to send to kafka. topic={}, payload={}", kafkaTopics.directMessageSent(), payload, exception);
            }
          });
    } catch (JsonProcessingException e) {
      throw KafkaMessageConvertException.withEvent(event.toString());
    }
  }

  @Async("eventExecutor")
  @KafkaListener(
      topics = "${spring.kafka.topics.direct-message-sent}",
      groupId = "#{T(java.util.UUID).randomUUID().toString()}"
  )
  public void handleDirectMessageReceivedEvent(String kafkaEvent) {
    DirectMessageReceivedEvent directMessageReceivedEvent = null;
    try {
      directMessageReceivedEvent = objectMapper.readValue(kafkaEvent,
          DirectMessageReceivedEvent.class);
    } catch (JsonProcessingException e) {
      throw KafkaMessageConvertException.withEvent(kafkaEvent);
    }

    DirectMessageDto dto = directMessageReceivedEvent.directMessageDto();
    String receiverId = dto.receiver().userId().toString();
    String senderId = dto.sender().userId().toString();

    String destination;

    if (receiverId.compareTo(senderId) < 0) {
      destination = String.format("/sub/direct-messages_%s_%s", receiverId, senderId);
    } else {
      destination = String.format("/sub/direct-messages_%s_%s", senderId, receiverId);
    }
    log.info("send direct message to {}. content={}", destination ,dto.content());
    messagingTemplate.convertAndSend(destination, dto);
  }
}
