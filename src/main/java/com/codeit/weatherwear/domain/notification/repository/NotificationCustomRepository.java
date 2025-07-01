package com.codeit.weatherwear.domain.notification.repository;

import com.codeit.weatherwear.domain.notification.NotificationDto;
import java.util.List;
import java.util.UUID;

public interface NotificationCustomRepository {

  List<NotificationDto> findNotification(UUID receiverId, String cursor, UUID idAfter, int limit);

}
