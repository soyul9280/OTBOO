package com.codeit.weatherwear.domain.event.dto;

import java.util.UUID;

public record DirectMessageReceivedEvent(
    UUID receiverId,
    String senderName,
    String content
) implements DomainEvent {

}
