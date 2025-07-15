package com.codeit.weatherwear.global.event.dto;

import com.codeit.weatherwear.domain.directmessage.dto.DirectMessageDto;

public record DirectMessageReceivedEvent(
    DirectMessageDto directMessageDto
) implements DomainEvent {

}
