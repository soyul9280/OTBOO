package com.codeit.weatherwear.global.event.publisher;

import com.codeit.weatherwear.global.event.dto.DirectMessageReceivedEvent;
import com.codeit.weatherwear.global.event.exception.KafkaMessageConvertException;
import com.codeit.weatherwear.global.properties.KafkaTopics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class DirectMessageKafkaPublisher {

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
}
