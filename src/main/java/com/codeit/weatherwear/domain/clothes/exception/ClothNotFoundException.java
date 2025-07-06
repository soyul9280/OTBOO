package com.codeit.weatherwear.domain.clothes.exception;

import com.codeit.weatherwear.global.exception.CustomException;
import com.codeit.weatherwear.global.exception.ErrorCode;

public class ClothNotFoundException extends CustomException {

  public ClothNotFoundException() {
    super(ErrorCode.CLOTH_NOT_FOUND);
  }
}
