package com.codeit.weatherwear.domain.user.dto.request;

import com.codeit.weatherwear.domain.user.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "권한 수정 정보")
public record UserRoleUpdateRequest(
    Role role
) {

}
