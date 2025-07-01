package com.codeit.weatherwear.domain.notification.repository;

import com.codeit.weatherwear.domain.notification.Notification;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID>, NotificationCustomRepository {

  long countByReceiverId(UUID receiverId);
}
