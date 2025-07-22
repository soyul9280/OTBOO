package com.codeit.weatherwear.domain.recommendation.exception;

import com.codeit.weatherwear.global.exception.CustomException;
import com.codeit.weatherwear.global.exception.ErrorCode;

public class GeminiParseException extends CustomException {

  public GeminiParseException() {
    super(ErrorCode.GEMINI_PARSE_ERROR);
  }
}
