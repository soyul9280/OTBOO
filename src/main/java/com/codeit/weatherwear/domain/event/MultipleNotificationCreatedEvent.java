package com.codeit.weatherwear.domain.event;

import com.codeit.weatherwear.domain.notification.NotificationDto;
import java.util.List;

public record MultipleNotificationCreatedEvent(
    List<NotificationDto> notificationDtos
) {

}
