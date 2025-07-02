package com.codeit.weatherwear.domain.auth.dto;

import jakarta.validation.constraints.Email;

public record ResetPasswordRequest(
    @Email
    String email
) {

}
