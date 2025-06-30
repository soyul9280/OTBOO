package com.codeit.weatherwear.domain.feed.repository.impl;

import com.codeit.weatherwear.domain.feed.dto.condition.FeedSearchCondition;
import com.codeit.weatherwear.domain.feed.entity.Feed;
import com.codeit.weatherwear.domain.feed.entity.QFeed;
import com.codeit.weatherwear.domain.feed.repository.FeedCustomRepository;
import com.codeit.weatherwear.domain.weather.entity.PrecipitationsType;
import com.codeit.weatherwear.domain.weather.entity.SkyStatus;
import com.codeit.weatherwear.global.request.SortDirection;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
public class FeedCustomRepositoryImpl implements FeedCustomRepository {

  private final JPAQueryFactory queryFactory;

  private static final String SORT_BY_CREATED_AT = "createdAt";
  private static final String SORT_BY_LIKE_COUNT = "likeCount";
  private static final List<String> ALLOWED_SORT_FIELDS = List.of(SORT_BY_CREATED_AT,
      SORT_BY_LIKE_COUNT);

  @Override
  public List<Feed> searchFeeds(FeedSearchCondition condition, int limit) {

    QFeed feed = QFeed.feed;

    return queryFactory
        .selectFrom(feed)
        .where(
            idAfter(feed, condition.getIdAfter(), condition.getCursor(), condition.getSortBy(),
                condition.getSortDirection()),
            keywordLike(condition.getKeywordLike()),
            skyStatusEqual(condition.getSkyStatusEqual()),
            precipitationTypeEqual(condition.getPrecipitationsTypeEqual()),
            authorIdEqual(condition.getAuthorIdEqual())
        )
        .orderBy(sortBy(condition.getSortBy(), condition.getSortDirection()))
        .limit(limit)
        .fetch();
  }

  private BooleanExpression idAfter(QFeed feed, UUID idAfter, String cursor, String sortBy,
      SortDirection direction) {

    if (idAfter == null || cursor == null) {
      return null;
    }

    switch (sortBy) {
      case SORT_BY_CREATED_AT -> {
        Instant cursorCreatedAt = Instant.parse(cursor);
        DateTimeExpression<Instant> createdAt = feed.createdAt;
        if (direction == SortDirection.ASCENDING) {
          return createdAt.gt(cursorCreatedAt)
              .or(createdAt.eq(cursorCreatedAt).and(feed.id.gt(idAfter)));
        } else {
          return createdAt.lt(cursorCreatedAt)
              .or(createdAt.eq(cursorCreatedAt).and(feed.id.lt(idAfter)));
        }
      }

      case SORT_BY_LIKE_COUNT -> {
        int cursorLikeCount = Integer.parseInt(cursor);
        NumberExpression<Integer> likeCount = feed.likeCount;
        if (direction == SortDirection.ASCENDING) {
          return likeCount.gt(cursorLikeCount)
              .or(likeCount.eq(cursorLikeCount).and(feed.id.gt(idAfter)));
        } else {
          return likeCount.lt(cursorLikeCount)
              .or(likeCount.eq(cursorLikeCount).and(feed.id.lt(idAfter)));
        }
      }
      default -> throw new UnsupportedOperationException("Sort field not implemented: " + sortBy);
    }
  }

  private BooleanExpression keywordLike(String keyword) {
    return StringUtils.hasText(keyword) ? QFeed.feed.content.containsIgnoreCase(keyword) : null;
  }

  private BooleanExpression skyStatusEqual(SkyStatus skyStatus) {
    return skyStatus != null ? QFeed.feed.weather.skyStatus.eq(skyStatus) : null;
  }

  private BooleanExpression precipitationTypeEqual(PrecipitationsType type) {
    return type != null ? QFeed.feed.weather.precipitation.type.eq(type) : null;
  }

  private BooleanExpression authorIdEqual(UUID authorId) {
    return authorId != null ? QFeed.feed.author.id.eq(authorId) : null;
  }

  private OrderSpecifier<?>[] sortBy(String sortBy, SortDirection direction) {

    if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
      log.warn("Unsupported Sort Field: {}", sortBy);
      // todo: Exception 생성 필요
      throw new IllegalArgumentException("Unsupported sort field");
    }

    PathBuilder<Feed> path = new PathBuilder<>(Feed.class, "feed");
    Order order = direction == SortDirection.ASCENDING ? Order.ASC : Order.DESC;

    OrderSpecifier<?> primarySort = switch (sortBy) {
      case SORT_BY_CREATED_AT ->
          new OrderSpecifier<>(order, path.getDateTime(SORT_BY_CREATED_AT, Instant.class));
      case SORT_BY_LIKE_COUNT ->
          new OrderSpecifier<>(order, path.getNumber(SORT_BY_LIKE_COUNT, Integer.class));
      default -> throw new UnsupportedOperationException("Sort field not implemented: " + sortBy);
    };

    OrderSpecifier<UUID> secondarySort = new OrderSpecifier<>(order, path.get("id", UUID.class));

    return new OrderSpecifier<?>[]{primarySort, secondarySort};
  }
}
