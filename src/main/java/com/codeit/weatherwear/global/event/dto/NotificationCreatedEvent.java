package com.codeit.weatherwear.global.event.dto;

import com.codeit.weatherwear.domain.notification.dto.NotificationDto;

public record NotificationCreatedEvent(
    NotificationDto notificationDto
) implements DomainEvent {

}
