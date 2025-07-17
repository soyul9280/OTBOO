package com.codeit.weatherwear.domain.directmessage.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record DirectMessageCreateRequest(
    @NotNull
    UUID receiverId,

    @NotNull
    UUID senderId,

    @NotBlank @Size(max = 255)
    String content
) {

}
