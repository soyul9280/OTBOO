package com.codeit.weatherwear.global.event.listener;

import com.codeit.weatherwear.domain.directmessage.dto.DirectMessageDto;
import com.codeit.weatherwear.global.event.dto.DirectMessageReceivedEvent;
import com.codeit.weatherwear.global.event.exception.KafkaMessageConvertException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DirectMessageKafkaListener {

  private final SimpMessagingTemplate messagingTemplate;
  private final ObjectMapper objectMapper;

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
