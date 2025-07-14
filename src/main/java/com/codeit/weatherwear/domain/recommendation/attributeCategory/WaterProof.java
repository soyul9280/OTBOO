package com.codeit.weatherwear.domain.recommendation.attributeCategory;

import java.util.Arrays;
import java.util.Optional;

public enum WaterProof {
  POSSIBLE("방수 가능"),
  NOT_POSSIBLE("방수 불가능");
  private final String label;

  WaterProof(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public static Optional<WaterProof> from(String label) {
    return Arrays.stream(values())
        .filter(waterProof -> waterProof.label.equalsIgnoreCase(label))
        .findFirst();
  }
}
