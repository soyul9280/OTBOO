package com.codeit.weatherwear.domain.recommendation.exception;

import com.codeit.weatherwear.global.exception.CustomException;
import com.codeit.weatherwear.global.exception.ErrorCode;

public class GeminiApiServerException extends CustomException {

  public GeminiApiServerException() {
    super(ErrorCode.GEMINI_API_SERVER);
  }
}
