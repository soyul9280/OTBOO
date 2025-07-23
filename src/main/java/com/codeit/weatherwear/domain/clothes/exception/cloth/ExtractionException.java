package com.codeit.weatherwear.domain.clothes.exception.cloth;

import com.codeit.weatherwear.global.exception.CustomException;
import com.codeit.weatherwear.global.exception.ErrorCode;
import java.util.Map;

public class ExtractionException extends CustomException {

  public ExtractionException(String url) {
    super(ErrorCode.CLOTH_EXTRACTION_ERROR, Map.of("url", url));
  }
}
