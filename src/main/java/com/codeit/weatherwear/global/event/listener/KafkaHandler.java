package com.codeit.weatherwear.global.event.listener;

import com.codeit.weatherwear.global.event.dto.ClothAttributeAddedEvent;
import com.codeit.weatherwear.global.event.dto.DirectMessageReceivedEvent;
import com.codeit.weatherwear.global.event.dto.FeedLikeEvent;
import com.codeit.weatherwear.global.event.dto.FolloweeFeedPostedEvent;
import com.codeit.weatherwear.global.event.dto.NewFeedCommentEvent;
import com.codeit.weatherwear.global.event.dto.NewFollowerEvent;
import com.codeit.weatherwear.global.event.dto.RoleChangedEvent;
import com.codeit.weatherwear.global.event.exception.KafkaMessageConvertException;
import com.codeit.weatherwear.global.properties.KafkaTopics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaHandler {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final KafkaTopics kafkaTopics;
  private final ObjectMapper objectMapper;

  @Async("eventExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleNewFollowerEvent(NewFollowerEvent event) {
    sendToKafka(kafkaTopics.newFollower(), event);
  }

  @Async("eventExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleClothAttributeAddedEvent(ClothAttributeAddedEvent event) {
    sendToKafka(kafkaTopics.clothAttributeAdded(), event);
  }

  @Async("eventExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleDirectMessageReceivedEvent(DirectMessageReceivedEvent event) {
    sendToKafka(kafkaTopics.directMessageReceived(), event);
  }

  @Async("eventExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleFeedLikeEvent(FeedLikeEvent event) {
    sendToKafka(kafkaTopics.feedLike(), event);
  }

  @Async("eventExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleNewFeedCommentEvent(NewFeedCommentEvent event) {
    sendToKafka(kafkaTopics.newFeedComment(), event);
  }

  @Async("eventExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleFolloweeFeedPostedEvent(FolloweeFeedPostedEvent event) {
    sendToKafka(kafkaTopics.followeeFeedPosted(), event);
  }

  @Async("eventExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleRoleChangedEvent(RoleChangedEvent event) {
    sendToKafka(kafkaTopics.roleChanged(), event);
  }

  private <T> void sendToKafka(String topic, T event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, payload);
      future.whenComplete((result, exception) -> {
        if (exception == null) {
          log.info("Produced event to topic {}: value = {}", result.getRecordMetadata().topic(), payload);
        } else {
          log.error("fail to send to kafka. topic={}, payload={}", topic, payload, exception);
        }
      });
    } catch (JsonProcessingException e) {
      log.error("fail to serialize {} to json. event={}", event.getClass().getSimpleName(), event, e);
      throw KafkaMessageConvertException.withEvent(event.toString());
    }
  }
}
