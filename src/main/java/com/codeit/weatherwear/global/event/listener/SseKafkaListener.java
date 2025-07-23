package com.codeit.weatherwear.global.event.listener;

import com.codeit.weatherwear.global.event.exception.KafkaMessageConvertException;
import com.codeit.weatherwear.global.sse.SseMessage;
import com.codeit.weatherwear.global.sse.SseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseKafkaListener {

  private final SseService sseService;
  private final ObjectMapper objectMapper;

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
}
