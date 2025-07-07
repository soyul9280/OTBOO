package com.codeit.weatherwear.domain.event.sse.dto;

import com.codeit.weatherwear.domain.notification.NotificationDto;

public record NotificationCreatedEvent(
    NotificationDto notificationDto
) {

}
