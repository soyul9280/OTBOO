package com.codeit.weatherwear.global.event.dto;

import java.util.UUID;

public record NewFeedCommentEvent(
    UUID receiverId,
    String authorName,
    String commentContent
) implements DomainEvent {

}
