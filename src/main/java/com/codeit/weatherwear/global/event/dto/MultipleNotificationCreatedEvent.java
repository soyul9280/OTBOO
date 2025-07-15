package com.codeit.weatherwear.global.event.dto;

import com.codeit.weatherwear.domain.notification.NotificationDto;
import java.util.List;

public record MultipleNotificationCreatedEvent(
    List<NotificationDto> notificationDtos
) implements DomainEvent {

}
