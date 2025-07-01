package com.codeit.weatherwear.domain.notification.repository;

import static com.codeit.weatherwear.domain.follow.QFollow.follow;
import static com.codeit.weatherwear.domain.notification.QNotification.notification;

import com.codeit.weatherwear.domain.notification.NotificationDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NotificationCustomRepositoryImpl implements NotificationCustomRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<NotificationDto> findNotification(UUID receiverId, String cursor, UUID idAfter,
      int limit) {
    List<NotificationDto> fetch = queryFactory
        .select(Projections.constructor(NotificationDto.class,
            notification.id, notification.createdAt, notification.receiverId,
            notification.title, notification.content, notification.level)
        )
        .from(notification)
        .where(
            notification.receiverId.eq(receiverId),
            cursor(cursor, idAfter)
        )
        .orderBy(
            notification.createdAt.desc(),
            notification.id.desc()
        )
        .limit(limit + 1)
        .fetch();

    if (fetch.size() > limit) {
      fetch.remove(fetch.size() - 1);
    }

    return fetch;
  }

  private BooleanExpression cursor(String cursor, UUID idAfter) {
    if (cursor == null || idAfter == null) {
      return null;
    }

    Instant createdAtCursor = Instant.parse(cursor);
    return follow.createdAt.lt(createdAtCursor)
        .or(follow.createdAt.eq(createdAtCursor).and(follow.id.lt(idAfter)));
  }
}
