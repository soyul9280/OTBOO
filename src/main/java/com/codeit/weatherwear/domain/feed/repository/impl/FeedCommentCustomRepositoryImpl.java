package com.codeit.weatherwear.domain.feed.repository.impl;

import com.codeit.weatherwear.domain.feed.dto.condition.FeedCommentSearchCondition;
import com.codeit.weatherwear.domain.feed.entity.Feed;
import com.codeit.weatherwear.domain.feed.entity.FeedComment;
import com.codeit.weatherwear.domain.feed.entity.QFeedComment;
import com.codeit.weatherwear.domain.feed.exception.NotImplementSortFieldException;
import com.codeit.weatherwear.domain.feed.exception.UnsupportedSortFieldException;
import com.codeit.weatherwear.domain.feed.repository.FeedCommentCustomRepository;
import com.codeit.weatherwear.domain.user.entity.QUser;
import com.codeit.weatherwear.global.request.SortDirection;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@Slf4j
@RequiredArgsConstructor
public class FeedCommentCustomRepositoryImpl implements FeedCommentCustomRepository {

  private final JPAQueryFactory queryFactory;

  private static final String SORT_BY_CREATED_AT = "createdAt";
  private static final List<String> ALLOWED_SORT_FIELDS = List.of(SORT_BY_CREATED_AT);

  @Override
  public Slice<FeedComment> searchFeedComments(FeedCommentSearchCondition condition) {
    QFeedComment feedComment = QFeedComment.feedComment;
    int limit = condition.getLimit();

    List<FeedComment> contents = queryFactory
        .selectFrom(feedComment)
        .join(feedComment.author, QUser.user).fetchJoin()
        .where(
            feedIdEqual(condition.getFeedId()),
            idAfter(feedComment, condition.getIdAfter(), condition.getCursor(),
                condition.getSortBy(), condition.getSortDirection())
        )
        .orderBy(sortBy(condition.getSortBy(), condition.getSortDirection()))
        .limit(limit + 1)
        .fetch();

    boolean hasNext = contents.size() > limit;
    if (hasNext) {
      contents.remove(limit);
    }

    return new SliceImpl<>(contents, PageRequest.of(0, limit), hasNext);
  }

  @Override
  public long getTotalFeedCommentCount(UUID feedId) {
    QFeedComment feedComment = QFeedComment.feedComment;
    Long result = queryFactory
        .select(feedComment.count())
        .from(feedComment)
        .where(feedComment.feed.id.eq(feedId))
        .fetchOne();

    return result != null ? result : 0L;
  }

  private BooleanExpression feedIdEqual(UUID feedId) {
    return feedId != null ? QFeedComment.feedComment.feed.id.eq(feedId) : null;
  }

  private BooleanExpression idAfter(QFeedComment feedComment, UUID idAfter, String cursor,
      String sortBy,
      SortDirection direction) {

    if (idAfter == null || cursor == null) {
      return null;
    }

    switch (sortBy) {
      case SORT_BY_CREATED_AT -> {
        Instant cursorCreatedAt = Instant.parse(cursor);
        DateTimeExpression<Instant> createdAt = feedComment.createdAt;
        if (direction == SortDirection.ASCENDING) {
          return createdAt.gt(cursorCreatedAt)
              .or(createdAt.eq(cursorCreatedAt).and(feedComment.id.gt(idAfter)));
        } else {
          return createdAt.lt(cursorCreatedAt)
              .or(createdAt.eq(cursorCreatedAt).and(feedComment.id.lt(idAfter)));
        }
      }
      default -> {
        log.warn("Not Implement Sort Field in Feed Comment: {}", sortBy);
        throw new NotImplementSortFieldException(sortBy);
      }
    }
  }

  private OrderSpecifier<?>[] sortBy(String sortBy, SortDirection direction) {

    if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
      log.warn("Unsupported Sort Field in Feed Comment: {}", sortBy);
      throw new UnsupportedSortFieldException(sortBy);
    }

    PathBuilder<Feed> path = new PathBuilder<>(Feed.class, "feed");
    Order order = direction == SortDirection.ASCENDING ? Order.ASC : Order.DESC;

    OrderSpecifier<?> primarySort = switch (sortBy) {
      case SORT_BY_CREATED_AT ->
          new OrderSpecifier<>(order, path.getDateTime(SORT_BY_CREATED_AT, Instant.class));
      default -> {
        log.warn("Not Implement Sort Field in Feed Comment: {}", sortBy);
        throw new NotImplementSortFieldException(sortBy);
      }
    };

    OrderSpecifier<UUID> secondarySort = new OrderSpecifier<>(order, path.get("id", UUID.class));

    return new OrderSpecifier<?>[]{primarySort, secondarySort};
  }

}
