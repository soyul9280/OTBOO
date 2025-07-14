package com.codeit.weatherwear.domain.clothes.exception.attribute;

import com.codeit.weatherwear.global.exception.CustomException;
import com.codeit.weatherwear.global.exception.ErrorCode;

public class AttributeNotFoundException extends CustomException {

  public AttributeNotFoundException() {
    super(ErrorCode.ATTRIBUTE_NOT_FOUND);
  }
}
