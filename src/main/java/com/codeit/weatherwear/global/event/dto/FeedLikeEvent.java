package com.codeit.weatherwear.global.event.dto;

import java.util.UUID;

public record FeedLikeEvent(
    UUID receiverId,
    String likerName,
    String feedContent
) implements DomainEvent {

}
