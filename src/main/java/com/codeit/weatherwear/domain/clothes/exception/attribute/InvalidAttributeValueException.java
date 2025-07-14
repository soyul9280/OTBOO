package com.codeit.weatherwear.domain.clothes.exception.attribute;

import com.codeit.weatherwear.global.exception.CustomException;
import com.codeit.weatherwear.global.exception.ErrorCode;

public class InvalidAttributeValueException extends CustomException {


  public InvalidAttributeValueException() {
    super(ErrorCode.INVALID_ATTRIBUTE_VALUE);
  }
}
