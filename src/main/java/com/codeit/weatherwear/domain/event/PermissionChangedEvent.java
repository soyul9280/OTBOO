package com.codeit.weatherwear.domain.event;

import com.codeit.weatherwear.domain.user.dto.response.UserDto;

public record PermissionChangedEvent(
    UserDto userDto
) {

}
