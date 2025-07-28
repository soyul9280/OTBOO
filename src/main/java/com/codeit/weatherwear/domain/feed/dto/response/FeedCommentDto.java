package com.codeit.weatherwear.domain.feed.dto.response;

import com.codeit.weatherwear.domain.follow.dto.UserSummaryDto;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FeedCommentDto {

  private UUID id;
  private Instant createdAt;
  private UUID feedId;
  private UserSummaryDto author;
  private String content;
}
