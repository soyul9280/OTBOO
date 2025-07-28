package com.codeit.weatherwear.domain.feed.dto.request;

import com.codeit.weatherwear.domain.feed.converter.EnumConverter;
import com.codeit.weatherwear.domain.feed.dto.condition.FeedSearchCondition;
import com.codeit.weatherwear.domain.weather.entity.PrecipitationsType;
import com.codeit.weatherwear.domain.weather.entity.SkyStatus;
import com.codeit.weatherwear.global.request.SortDirection;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Locale;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FeedGetParamRequest {

  private String cursor;
  private UUID idAfter;

  @NotNull(message = "limit는 필수입니다.")
  @Min(value = 1, message = "limit는 1 이상이어야 합니다.")
  private Integer limit;

  @NotBlank(message = "sortBy는 필수입니다.")
  private String sortBy;

  @NotBlank(message = "sortDirection은 필수입니다.")
  private String sortDirection;

  private String keywordLike;
  private String skyStatusEqual;
  private String precipitationTypeEqual;
  private UUID authorIdEqual;

  public FeedSearchCondition toSearchCondition() {
    return FeedSearchCondition.builder()
        .cursor(this.cursor)
        .idAfter(this.getIdAfter())
        .limit(this.getLimit())
        .sortBy(this.sortBy)
        .sortDirection(
            EnumConverter.toEnum(this.getSortDirection(), SortDirection.class, "sortDirection",
                Locale.ENGLISH))
        .keywordLike(this.getKeywordLike())
        .skyStatusEqual(
            EnumConverter.toEnum(this.getSkyStatusEqual(), SkyStatus.class, "skyStatusEqual",
                Locale.ENGLISH))
        .precipitationsTypeEqual(
            EnumConverter.toEnum(this.getPrecipitationTypeEqual(), PrecipitationsType.class,
                "precipitationTypeEqual", Locale.ENGLISH))
        .authorIdEqual(this.getAuthorIdEqual())
        .build();
  }

}
