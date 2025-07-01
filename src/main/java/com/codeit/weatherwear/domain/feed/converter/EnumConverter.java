package com.codeit.weatherwear.domain.feed.converter;

import com.codeit.weatherwear.domain.feed.exception.InvalidEnumFieldValueException;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnumConverter {

  public static <T extends Enum<T>> T toEnum(String value, Class<T> enumClass, String fieldName,
      Locale locale) {
    if (value == null) {
      return null;
    }

    try {
      return Enum.valueOf(enumClass, value.toUpperCase(locale));
    } catch (IllegalArgumentException e) {
      log.warn("Invalid {}: {}", fieldName, value);
      throw new InvalidEnumFieldValueException(fieldName, value);
    }

  }

}
