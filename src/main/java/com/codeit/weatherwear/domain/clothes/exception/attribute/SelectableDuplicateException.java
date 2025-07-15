package com.codeit.weatherwear.domain.clothes.exception.attribute;

import com.codeit.weatherwear.global.exception.CustomException;
import com.codeit.weatherwear.global.exception.ErrorCode;

public class SelectableDuplicateException extends CustomException {

  public SelectableDuplicateException() {
    super(ErrorCode.SELECTABLE_DUPLICATE);
  }
}
