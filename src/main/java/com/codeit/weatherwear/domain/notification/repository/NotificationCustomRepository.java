package com.codeit.weatherwear.domain.notification.repository;

import com.codeit.weatherwear.domain.notification.NotificationDto;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface NotificationCustomRepository {

  Slice<NotificationDto> findNotification(UUID receiverId, String cursor, UUID idAfter, Pageable pageable);

}
