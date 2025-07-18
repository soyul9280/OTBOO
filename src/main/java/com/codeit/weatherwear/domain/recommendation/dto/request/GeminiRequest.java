package com.codeit.weatherwear.domain.recommendation.dto.request;

import java.util.List;

public record GeminiRequest(
    List<Content> content
) {

  public record Content(
      List<Part> parts
  ) {

  }

  public record Part(
      String text
  ) {

  }
}
