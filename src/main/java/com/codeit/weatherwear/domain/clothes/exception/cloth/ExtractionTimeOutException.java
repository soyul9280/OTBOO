package com.codeit.weatherwear.domain.clothes.exception.cloth;

import com.codeit.weatherwear.global.exception.CustomException;
import com.codeit.weatherwear.global.exception.ErrorCode;
import java.util.Map;

public class ExtractionTimeOutException extends CustomException {

  public ExtractionTimeOutException(String url) {
    super(ErrorCode.CLOTH_EXTRACTION_TIME_OUT, Map.of("url", url));
  }
}
