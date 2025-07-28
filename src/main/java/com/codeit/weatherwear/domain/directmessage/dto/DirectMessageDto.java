package com.codeit.weatherwear.domain.directmessage.dto;

import com.codeit.weatherwear.domain.directmessage.DirectMessage;
import com.codeit.weatherwear.domain.follow.dto.UserSummaryDto;
import java.time.Instant;
import java.util.UUID;

public record DirectMessageDto (
    UUID id,
    Instant createdAt,
    UserSummaryDto sender,
    UserSummaryDto receiver,
    String content
) {

  public static DirectMessageDto from(DirectMessage directMessage) {
    return new DirectMessageDto(
        directMessage.getId(),
        directMessage.getCreatedAt(),
        UserSummaryDto.from(directMessage.getSender()),
        UserSummaryDto.from(directMessage.getReceiver()),
        directMessage.getContent()
    );
  }
}
