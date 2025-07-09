package com.codeit.weatherwear.domain.event.dto;

import com.codeit.weatherwear.domain.directmessage.dto.DirectMessageDto;

public record DirectMessageReceivedEvent(
    DirectMessageDto directMessageDto
) implements DomainEvent {

}
