package com.codeit.weatherwear.global.event.dto;

import com.codeit.weatherwear.domain.user.entity.Role;
import java.util.UUID;

public record RoleChangedEvent(
    UUID receiverId,
    Role newRoles,
    Role previousRoles
) implements DomainEvent {

}
