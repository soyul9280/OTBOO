package com.codeit.weatherwear.domain.clothes.exception.cloth;

import com.codeit.weatherwear.global.exception.CustomException;
import com.codeit.weatherwear.global.exception.ErrorCode;
import java.util.Map;

public class NotSupportSiteException extends CustomException {

  public NotSupportSiteException(String url) {
    super(ErrorCode.NOT_SUPPORT_SITE, Map.of("url", url));
  }
}
