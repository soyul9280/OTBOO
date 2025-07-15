package com.codeit.weatherwear.domain.recommendation.attributeCategory;

import java.util.Arrays;
import java.util.Optional;

public enum Season {
  SPRING("봄"),
  SUMMER("여름"),
  FALL("가을"),
  WINTER("겨울");

  private final String label;

  Season(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public static Optional<Season> from(String label) {
    return Arrays.stream(values())
        .filter(season -> season.label.equalsIgnoreCase(label))
        .findFirst();
  }
}
