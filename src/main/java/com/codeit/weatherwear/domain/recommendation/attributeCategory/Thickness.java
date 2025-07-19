package com.codeit.weatherwear.domain.recommendation.attributeCategory;

import java.util.Arrays;
import java.util.Optional;

public enum Thickness {
  VERY_THICK("아주 두꺼움"),
  THICK("두꺼움"),
  LIGHT("얇음"),
  VERY_LIGHT("아주 얇음");

  private final String label;

  Thickness(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public static Optional<Thickness> from(String label) {
    return Arrays.stream(values())
        .filter(thickness -> thickness.label.equalsIgnoreCase(label))
        .findFirst();
  }
}
