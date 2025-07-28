package com.codeit.weatherwear.domain.clothes.exception.cloth;

import com.codeit.weatherwear.global.exception.CustomException;
import com.codeit.weatherwear.global.exception.ErrorCode;

public class ExtractionNotFoundException extends CustomException {

  public ExtractionNotFoundException() {
    super(ErrorCode.CLOTH_EXTRACTION_NOT_FOUND_ELEMENT);
  }
}
