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
import com.codeit.weatherwear.global.event.dto.WeatherAlertEvent;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

  private final NotificationService notificationService;

  @Async("eventExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleNewFollowerEvent(NewFollowerEvent event) {

    String title = String.format("%s님이 나를 팔로우 했어요.", event.followerName());
    String content = "";

    notificationService.create(
        event.receiverId(),
        title,
        content,
        Level.INFO
    );
  }

  @Async("eventExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleClothAttributeAddedEvent(ClothAttributeAddedEvent event) {
    String attributeName = event.attributeName();

    String title = "새로운 의상 속상이 추가되었어요.";
    String content = String.format("내 의상에 [%s] 속성을 추가해보세요.", attributeName);

    notificationService.createAllUser(
        title,
        content,
        Level.INFO
    );
  }

  @Async("eventExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleDirectMessageReceivedEvent(DirectMessageReceivedEvent event) {
    DirectMessageDto dto = event.directMessageDto();
    UUID receiverId = dto.receiver().userId();
    String senderName = dto.sender().name();

    String title = String.format("[DM] %s", senderName);
    String content = event.directMessageDto().content();

    notificationService.create(
        receiverId,
        title,
        content,
        Level.INFO
    );
  }

  @Async("eventExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleFeedLikeEvent(FeedLikeEvent event) {
    String title = String.format("%s님이 내 피드를 좋아합니다.", event.likerName());
    String content = event.feedContent();

    notificationService.create(
        event.receiverId(),
        title,
        content,
        Level.INFO
    );
  }

  @Async("eventExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleNewFeedCommentEvent(NewFeedCommentEvent event) {
    String title = String.format("%s님이 댓글을 달았어요.", event.authorName());
    String content = event.commentContent();

    notificationService.create(
        event.receiverId(),
        title,
        content,
        Level.INFO
    );
  }

  @Async("eventExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleFolloweeFeedPostedEvent(FolloweeFeedPostedEvent event) {
    String title = String.format("%s님이 새로운 피드를 작성했어요.", event.followeeName());
    String content = event.content();

    notificationService.create(
        event.receiverIds(),
        title,
        content,
        Level.INFO
    );
  }

  @Async("eventExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handlePermissionChangedEvent(RoleChangedEvent event) {
    String title = "권한이 변경되었어요.";
    String content = String.format("%s -> %s", event.previousRoles().name(),
        event.newRoles().name());

    notificationService.create(
        event.receiverId(),
        title,
        content,
        Level.INFO
    );
  }

  @Async("eventExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleWeatherEvent(WeatherAlertEvent event) {
    String title = String.format("%s의 날씨 특이사항 알림", event.address());
    String content = event.content();

    notificationService.create(
        event.receiverIds(),
        title,
        content,
        Level.INFO
    );
  }
}
