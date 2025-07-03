package com.codeit.weatherwear.domain.clothes.exception;

import com.codeit.weatherwear.global.exception.CustomException;
import com.codeit.weatherwear.global.exception.ErrorCode;

public class ClothAlreadyExistsException extends CustomException {

  public ClothAlreadyExistsException() {
    super(ErrorCode.CLOTH_ALREADY_EXISTS);
  }
}
