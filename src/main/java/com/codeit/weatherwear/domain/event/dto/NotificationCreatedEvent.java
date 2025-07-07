package com.codeit.weatherwear.domain.event.dto;

import com.codeit.weatherwear.domain.notification.NotificationDto;

public record NotificationCreatedEvent(
    NotificationDto notificationDto
) implements DomainEvent {

}
