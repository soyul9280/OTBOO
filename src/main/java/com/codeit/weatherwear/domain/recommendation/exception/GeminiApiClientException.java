package com.codeit.weatherwear.domain.recommendation.exception;

import com.codeit.weatherwear.global.exception.CustomException;
import com.codeit.weatherwear.global.exception.ErrorCode;

public class GeminiApiClientException extends CustomException {

  public GeminiApiClientException() {
    super(ErrorCode.GEMINI_API_CLIENT);
  }
}
