package com.codeit.weatherwear.domain.clothes.exception.attribute;

import com.codeit.weatherwear.global.exception.CustomException;
import com.codeit.weatherwear.global.exception.ErrorCode;

public class AttributeAlreadyExistsException extends CustomException {

  public AttributeAlreadyExistsException() {
    super(ErrorCode.ATTRIBUTE_ALREADY_EXISTS);
  }
}
