package com.codeit.weatherwear.domain.feed.exception;

import com.codeit.weatherwear.global.exception.CustomException;
import com.codeit.weatherwear.global.exception.ErrorCode;
import java.util.Map;

public class InvalidEnumFieldValueException extends CustomException {

  public InvalidEnumFieldValueException(String fieldName, String value) {
    super(ErrorCode.INVALID_ENUM_VALUE, Map.of(fieldName, value));
  }
}
