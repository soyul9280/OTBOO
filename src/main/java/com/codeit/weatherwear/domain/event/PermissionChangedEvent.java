package com.codeit.weatherwear.domain.event;

import com.codeit.weatherwear.domain.user.dto.response.UserDto;
import com.codeit.weatherwear.domain.user.entity.Role;

public record PermissionChangedEvent(
    UserDto userDto,
    Role previousRoles
) {

}
