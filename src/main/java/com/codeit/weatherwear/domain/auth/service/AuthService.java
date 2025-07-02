package com.codeit.weatherwear.domain.auth.service;

import com.codeit.weatherwear.domain.auth.dto.ResetPasswordRequest;

public interface AuthService {

  void resetPassword(ResetPasswordRequest resetPasswordRequest);
}
