package com.codeit.weatherwear.global.event.exception;

import com.codeit.weatherwear.global.exception.CustomException;
import com.codeit.weatherwear.global.exception.ErrorCode;
import java.util.Map;

public class KafkaMessageConvertException extends CustomException {

  public KafkaMessageConvertException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static KafkaMessageConvertException withEvent(String event) {
    return new KafkaMessageConvertException(ErrorCode.KAFKA_MESSAGE_CONVERT_FAIL, Map.of("event", event));
  }

}
