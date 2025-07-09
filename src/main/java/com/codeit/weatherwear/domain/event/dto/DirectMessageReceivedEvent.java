package com.codeit.weatherwear.domain.event.dto;

import com.codeit.weatherwear.domain.directmessage.dto.DirectMessageDto;
import java.util.UUID;

public record DirectMessageReceivedEvent(
    UUID receiverId,
    UUID senderId,
    String senderName,
    DirectMessageDto directMessageDto
) implements DomainEvent {

}
