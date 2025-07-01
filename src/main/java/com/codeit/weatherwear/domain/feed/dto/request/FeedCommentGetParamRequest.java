package com.codeit.weatherwear.domain.feed.dto.request;

import com.codeit.weatherwear.domain.feed.dto.condition.FeedCommentSearchCondition;
import com.codeit.weatherwear.global.request.SortDirection;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FeedCommentGetParamRequest {

  @NotNull(message = "대상 Feed Id는 필수입니다.")
  private UUID feedId;

  private String cursor;
  private UUID idAfter;

  @NotNull(message = "limit는 필수입니다.")
  @Min(value = 1, message = "limit는 1 이상이어야 합니다.")
  private Integer limit;

  public FeedCommentSearchCondition toSearchCondition() {
    return FeedCommentSearchCondition.builder()
        .feedId(this.getFeedId())
        .cursor(this.getCursor())
        .idAfter(this.getIdAfter())
        .limit(this.getLimit())
        .sortBy("createdAt")
        .sortDirection(SortDirection.DESCENDING)
        .build();
  }
}
