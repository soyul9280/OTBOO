package com.codeit.weatherwear.domain.recommendation.attributeCategory;

public enum AttributeType {
  SEASON("계절"),
  THICKNESS("두께"),
  WATERPROOF("방수");

  private final String key;

  AttributeType(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }
}
