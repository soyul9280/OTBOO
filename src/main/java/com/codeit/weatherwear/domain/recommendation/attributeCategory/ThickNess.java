package com.codeit.weatherwear.domain.recommendation.attributeCategory;

import java.util.Arrays;
import java.util.Optional;

public enum ThickNess {
  VERY_THICK("아주 두꺼움"),
  THICK("두꺼움"),
  THICK_NESS("얇음"),
  VERY_THICK_NESS("아주 얇음");

  private final String label;

  ThickNess(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public static Optional<ThickNess> from(String label) {
    return Arrays.stream(values())
        .filter(thickNess -> thickNess.label.equalsIgnoreCase(label))
        .findFirst();
  }
}
