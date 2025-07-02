package com.codeit.weatherwear.domain.clothes.exception;

import com.codeit.weatherwear.global.exception.CustomException;
import com.codeit.weatherwear.global.exception.ErrorCode;

public class AttributeAlreadyExistsException extends CustomException {

  public AttributeAlreadyExistsException() {
    super(ErrorCode.ATTRIBUTE_ALREADY_EXISTS);
  }
}
