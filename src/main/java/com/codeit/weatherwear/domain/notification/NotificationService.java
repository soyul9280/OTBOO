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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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

  @Transactional
  public List<NotificationDto> create(List<UUID> receiverIds, String title, String content, Level level) {
    List<Notification> notifications = receiverIds.stream()
        .map(receiverId -> Notification.create(receiverId, title, content, level))
        .toList();

    notificationRepository.saveAll(notifications);
    return notifications.stream()
        .map(NotificationDto::from)
        .toList();
  }

  public PageResponse<NotificationDto> findNotification(UUID receiverId, String cursor, UUID idAfter, Pageable pageable) {
    Slice<NotificationDto> dtos = notificationRepository
        .findNotification(receiverId, cursor, idAfter, pageable);
    long totalCount = notificationRepository.countByReceiverId(receiverId);
    return toPageResponse(dtos, totalCount);
  }

  @Transactional
  public void delete(UUID id) {
    notificationRepository.findById(id).ifPresent(notification -> {
      notificationRepository.delete(notification);
      log.info("알림 삭제. id={}", id);
    });
  }

  private PageResponse<NotificationDto> toPageResponse(Slice<NotificationDto> notifications, long totalCount) {
    List<NotificationDto> content = notifications.getContent();
    boolean hasNext = notifications.hasNext();
    Instant nextCursor = null;
    UUID nextIdAfter = null;

    if (hasNext) {
      NotificationDto notification = content.get(content.size() - 1);

      nextCursor = notification.createdAt();
      nextIdAfter = notification.id();
    }
    String sortBy = "createdAt";

    return new PageResponse<>(
        content,
        nextCursor,
        nextIdAfter,
        hasNext,
        totalCount,
        sortBy,
        SortDirection.DESCENDING.name()
    );
  }
}
