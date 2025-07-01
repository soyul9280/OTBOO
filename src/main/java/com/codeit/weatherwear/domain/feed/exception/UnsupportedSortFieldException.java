package com.codeit.weatherwear.domain.feed.exception;

import com.codeit.weatherwear.global.exception.CustomException;
import com.codeit.weatherwear.global.exception.ErrorCode;
import java.util.Map;

public class UnsupportedSortFieldException extends CustomException {

  public UnsupportedSortFieldException(String sortBy) {
    super(ErrorCode.UNSUPPORTED_SORT_FIELD, Map.of("sortField", sortBy));
  }
}
