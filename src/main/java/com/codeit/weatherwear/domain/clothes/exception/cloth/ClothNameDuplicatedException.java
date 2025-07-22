package com.codeit.weatherwear.domain.clothes.exception.cloth;

import com.codeit.weatherwear.global.exception.CustomException;
import com.codeit.weatherwear.global.exception.ErrorCode;
import java.util.Map;

public class ClothNameDuplicatedException extends CustomException {

  public ClothNameDuplicatedException(String name) {
    super(ErrorCode.CLOTH_NAME_DUPLICATED, Map.of("name", name));
  }
}
