package com.codeit.weatherwear.domain.recommendation.dto.response;

import java.util.List;

public record GeminiResponse(
    List<Candidate> candidates,
    UsageMetadata usageMetadata,
    String modelVersion,
    String responseId
) {

  public record Candidate(
      Content content,
      String finishReason,
      int index
  ) {

  }

  public record Content(
      List<Part> parts,
      String role
  ) {

  }

  public record Part(
      String text
  ) {

  }

  public record UsageMetadata(
      int promptTokenCount,
      int candidatesTokenCount,
      int totaltokenCount,
      List<PromptTokensDetail> promptTokensDetails,
      int thoughtsTokenCount
  ) {

  }

  public record PromptTokensDetail(
      String modality,
      int tokenCount
  ) {

  }
}