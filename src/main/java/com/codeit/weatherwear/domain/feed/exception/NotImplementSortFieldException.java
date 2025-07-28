package com.codeit.weatherwear.domain.feed.exception;

import com.codeit.weatherwear.global.exception.CustomException;
import com.codeit.weatherwear.global.exception.ErrorCode;
import java.util.Map;

public class NotImplementSortFieldException extends CustomException {

  public NotImplementSortFieldException(String sortBy) {
    super(ErrorCode.NOT_IMPLEMENTED_SORT_FIELD, Map.of("sortField", sortBy));
  }
}
