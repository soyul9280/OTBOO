package com.codeit.weatherwear.domain.notification.repository;

import static com.codeit.weatherwear.domain.notification.QNotification.notification;

import com.codeit.weatherwear.domain.notification.dto.NotificationDto;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@RequiredArgsConstructor
public class NotificationCustomRepositoryImpl implements NotificationCustomRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Slice<NotificationDto> findNotification(UUID receiverId, String cursor, UUID idAfter,
      Pageable pageable) {
    List<NotificationDto> data = queryFactory
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
        .limit(pageable.getPageSize() + 1)
        .fetch();

    boolean hasNext = data.size() > pageable.getPageSize();
    if (hasNext) {
      data.remove(data.size() - 1);
    }

    return new SliceImpl<>(data, pageable, hasNext);
  }

  private BooleanExpression cursor(String cursor, UUID idAfter) {
    if (cursor == null || idAfter == null) {
      return null;
    }

    Instant createdAtCursor = Instant.parse(cursor);
    return notification.createdAt.lt(createdAtCursor)
        .or(notification.createdAt.eq(createdAtCursor).and(notification.id.lt(idAfter)));
  }
}
