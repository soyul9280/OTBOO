package com.codeit.weatherwear.domain.event;

import com.codeit.weatherwear.domain.user.entity.Role;
import java.util.UUID;

public record PermissionChangedEvent(
    UUID receiverId,
    Role newRoles,
    Role previousRoles
) {

}
