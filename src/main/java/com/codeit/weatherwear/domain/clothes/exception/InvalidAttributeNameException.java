package com.codeit.weatherwear.domain.clothes.exception;

import com.codeit.weatherwear.global.exception.CustomException;
import com.codeit.weatherwear.global.exception.ErrorCode;

public class InvalidAttributeNameException extends CustomException {


  public InvalidAttributeNameException() {
    super(ErrorCode.INVALID_ATTRIBUTE_NAME);
  }
}
