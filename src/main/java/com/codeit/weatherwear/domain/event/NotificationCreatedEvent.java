package com.codeit.weatherwear.domain.event;

import com.codeit.weatherwear.domain.notification.NotificationDto;

public record NotificationCreatedEvent(
    NotificationDto notificationDto
) {

}
