package com.codeit.weatherwear.global.event.dto;

import java.util.UUID;

public record NewFollowerEvent(
    UUID receiverId,
    String followerName
) implements DomainEvent {

}
