package com.codeit.weatherwear.global.event.listener;

import com.codeit.weatherwear.domain.directmessage.dto.DirectMessageDto;
import com.codeit.weatherwear.domain.notification.Notification.Level;
import com.codeit.weatherwear.domain.notification.NotificationService;
import com.codeit.weatherwear.global.event.dto.ClothAttributeAddedEvent;
import com.codeit.weatherwear.global.event.dto.DirectMessageReceivedEvent;
import com.codeit.weatherwear.global.event.dto.FeedLikeEvent;
import com.codeit.weatherwear.global.event.dto.FolloweeFeedPostedEvent;
import com.codeit.weatherwear.global.event.dto.NewFeedCommentEvent;
import com.codeit.weatherwear.global.event.dto.NewFollowerEvent;
import com.codeit.weatherwear.global.event.dto.RoleChangedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

  private final NotificationService notificationService;

  private final ObjectMapper objectMapper;

  @Async("eventExecutor")
  @KafkaListener(topics = "weatherwear.new_follower")
  public void handleNewFollowerEvent(String kafkaEvent) throws JsonProcessingException {
    NewFollowerEvent newFollowerEvent = objectMapper.readValue(kafkaEvent, NewFollowerEvent.class);

    String title = String.format("%s님이 나를 팔로우 했어요.", newFollowerEvent.followerName());
    String content = "";

    notificationService.create(
        newFollowerEvent.receiverId(),
        title,
        content,
        Level.INFO
    );
  }

  @Async("eventExecutor")
  @KafkaListener(topics = "weatherwear.cloth_attribute_added")
  public void handleClothAttributeAddedEvent(String kafkaEvent) throws JsonProcessingException {
    ClothAttributeAddedEvent clothAttributeAddedEvent = objectMapper.readValue(kafkaEvent,
        ClothAttributeAddedEvent.class);

    String attributeName = clothAttributeAddedEvent.attributeName();

    String title = "새로운 의상 속상이 추가되었어요.";
    String content = String.format("내 의상에 [%s] 속성을 추가해보세요.", attributeName);

    notificationService.createAllUser(
        title,
        content,
        Level.INFO
    );
  }

  @Async("eventExecutor")
  @KafkaListener(topics = "weatherwear.direct_message_received")
  public void handleDirectMessageReceivedEvent(String kafkaEvent) throws JsonProcessingException {
    DirectMessageReceivedEvent directMessageReceivedEvent = objectMapper.readValue(kafkaEvent,
        DirectMessageReceivedEvent.class);

    DirectMessageDto dto = directMessageReceivedEvent.directMessageDto();
    UUID receiverId = dto.receiver().userId();
    String senderName = dto.sender().name();

    String title = String.format("[DM] %s", senderName);
    String content = directMessageReceivedEvent.directMessageDto().content();

    notificationService.create(
        receiverId,
        title,
        content,
        Level.INFO
    );
  }

  @Async("eventExecutor")
  @KafkaListener(topics = "weatherwear.feed_like")
  public void handleFeedLikeEvent(String kafkaEvent) throws JsonProcessingException {
    FeedLikeEvent feedLikeEvent = objectMapper.readValue(kafkaEvent, FeedLikeEvent.class);

    String title = String.format("%s님이 내 피드를 좋아합니다.", feedLikeEvent.likerName());
    String content = feedLikeEvent.feedContent();

    notificationService.create(
        feedLikeEvent.receiverId(),
        title,
        content,
        Level.INFO
    );
  }

  @Async("eventExecutor")
  @KafkaListener(topics = "weatherwear.new_feed_comment")
  public void handleNewFeedCommentEvent(String kafkaEvent) throws JsonProcessingException {
    NewFeedCommentEvent newFeedCommentEvent = objectMapper.readValue(kafkaEvent, NewFeedCommentEvent.class);

    String title = String.format("%s님이 댓글을 달았어요.", newFeedCommentEvent.authorName());
    String content = newFeedCommentEvent.commentContent();

    notificationService.create(
        newFeedCommentEvent.receiverId(),
        title,
        content,
        Level.INFO
    );
  }

  @Async("eventExecutor")
  @KafkaListener(topics = "weatherwear.followee_feed_posted")
  public void handleFolloweeFeedPostedEvent(String kafkaEvent) throws JsonProcessingException {
    FolloweeFeedPostedEvent followeeFeedPostedEvent = objectMapper.readValue(kafkaEvent,
        FolloweeFeedPostedEvent.class);

    String title = String.format("%s님이 새로운 피드를 작성했어요.", followeeFeedPostedEvent.followeeName());
    String content = followeeFeedPostedEvent.content();

    notificationService.create(
        followeeFeedPostedEvent.receiverIds(),
        title,
        content,
        Level.INFO
    );
  }

  @Async("eventExecutor")
  @KafkaListener(topics = "weatherwear.role_changed")
  public void handlePermissionChangedEvent(String kafkaEvent) throws JsonProcessingException {
    RoleChangedEvent roleChangedEvent = objectMapper.readValue(kafkaEvent, RoleChangedEvent.class);

    String title = "권한이 변경되었어요.";
    String content = String.format("%s -> %s", roleChangedEvent.previousRoles().name(), roleChangedEvent.newRoles().name());

    notificationService.create(
        roleChangedEvent.receiverId(),
        title,
        content,
        Level.INFO
    );
  }
}
