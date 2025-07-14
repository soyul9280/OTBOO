package com.codeit.weatherwear.domain.clothes.exception.cloth;

import com.codeit.weatherwear.global.exception.CustomException;
import com.codeit.weatherwear.global.exception.ErrorCode;
import java.util.Map;

public class ExtractionNotFoundException extends CustomException {

  public ExtractionNotFoundException(String url) {
    super(ErrorCode.CLOTH_EXTRACTION_NOT_FOUND_ELEMENT, Map.of("url", url));
  }
}
