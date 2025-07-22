package com.codeit.weatherwear.global.event.dto;

import java.util.List;
import java.util.UUID;

public record WeatherAlertEvent(
    List<UUID> receiverIds,
    String address,
    String content
) implements DomainEvent {

}
