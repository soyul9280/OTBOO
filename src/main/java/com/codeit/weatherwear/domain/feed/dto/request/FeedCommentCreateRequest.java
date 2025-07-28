package com.codeit.weatherwear.domain.feed.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FeedCommentCreateRequest {

  @NotNull
  private UUID feedId;

  @NotNull
  private UUID authorId;

  @NotEmpty
  private String content;
}
