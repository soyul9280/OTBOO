package com.codeit.weatherwear.domain.notification;

import com.codeit.weatherwear.domain.notification.Notification.Level;
import com.codeit.weatherwear.domain.notification.repository.NotificationRepository;
import com.codeit.weatherwear.domain.user.exception.UserNotFoundException;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import com.codeit.weatherwear.global.request.SortDirection;
import com.codeit.weatherwear.global.response.PageResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;

  @Transactional
  public NotificationDto create(UUID receiverId, String title, String content, Level level) {
    if (!userRepository.existsById(receiverId)) {
      throw new UserNotFoundException();
    }

    Notification notification = notificationRepository
        .save(Notification.create(receiverId, title, content, level));
    log.info("알림 생성. id={}", notification.getId());
    return NotificationDto.from(notification);
  }

  public PageResponse<NotificationDto> findNotification(UUID receiverId, String cursor, UUID idAfter, int limit) {
    List<NotificationDto> dtos = notificationRepository
        .findNotification(receiverId, cursor, idAfter, limit);
    long totalCount = notificationRepository.countByReceiverId(receiverId);
    return toPageResponse(dtos, limit, totalCount);
  }

  @Transactional
  public void delete(UUID id) {
    notificationRepository.findById(id).ifPresent(notification -> {
      notificationRepository.delete(notification);
      log.info("알림 삭제. id={}", id);
    });
  }

  private PageResponse<NotificationDto> toPageResponse(List<NotificationDto> notifications, int limit, long totalCount) {
    boolean hasNext = notifications.size() > limit;
    Instant nextCursor = null;
    UUID nextIdAfter = null;

    if (hasNext) {
      NotificationDto notification = notifications.get(notifications.size() - 1);

      nextCursor = notification.createdAt();
      nextIdAfter = notification.id();
    }
    String sortBy = "createdAt";

    return new PageResponse<>(
        notifications,
        nextCursor,
        nextIdAfter,
        hasNext,
        totalCount,
        sortBy,
        SortDirection.DESCENDING.name()
    );
  }
}
