package com.codeit.weatherwear.domain.security.exception;

import com.codeit.weatherwear.global.exception.CustomException;
import com.codeit.weatherwear.global.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class AccountLockedException extends CustomException {

  public AccountLockedException() {
    super(ErrorCode.ACCOUNT_LOCKED);
  }

  public AccountLockedException(UUID userId) {
    super(ErrorCode.ACCOUNT_LOCKED, Map.of("userId", userId));
  }
}
