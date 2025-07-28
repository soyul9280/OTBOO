package com.codeit.weatherwear.domain.feed.dto.condition;

import com.codeit.weatherwear.global.request.SortDirection;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FeedCommentSearchCondition {

  private UUID feedId;
  private String cursor;
  private UUID idAfter;
  private int limit;

  private String sortBy;
  private SortDirection sortDirection;
}
