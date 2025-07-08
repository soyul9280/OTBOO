package com.codeit.weatherwear.domain.directmessage.repository;

import static com.codeit.weatherwear.domain.directmessage.QDirectMessage.directMessage;

import com.codeit.weatherwear.domain.directmessage.DirectMessageDto;
import com.codeit.weatherwear.domain.follow.dto.UserSummaryDto;
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
public class DirectMessageCustomRepositoryImpl implements DirectMessageCustomRepository{

  private final JPAQueryFactory queryFactory;

  @Override
  public Slice<DirectMessageDto> findDirectMessages(
      UUID myId, UUID peerId, String cursor, UUID idAfter, Pageable pageable) {
    List<DirectMessageDto> data = queryFactory
        .select(Projections.constructor(DirectMessageDto.class,
            directMessage.id,
            directMessage.createdAt,
            Projections.constructor(UserSummaryDto.class,
                directMessage.sender.id,
                directMessage.sender.name,
                directMessage.sender.profileImageUrl
            ),
            Projections.constructor(UserSummaryDto.class,
                directMessage.receiver.id,
                directMessage.receiver.name,
                directMessage.receiver.profileImageUrl
            ),
            directMessage.content
        ))
        .from(directMessage)
        .where(
            cursor(cursor, idAfter),
            conversationBetween(myId, peerId)
        )
        .orderBy(directMessage.createdAt.desc(), directMessage.id.desc())
        .limit(pageable.getPageSize() + 1)
        .fetch();

    boolean hasNext = data.size() > pageable.getPageSize();
    if (hasNext) {
      data.remove(data.size() - 1);
    }
    return new SliceImpl<>(data, pageable, hasNext);
  }

  @Override
  public long getTotalCount(UUID myId, UUID peerId) {
    Long count = queryFactory
        .select(directMessage.count())
        .from(directMessage)
        .where(conversationBetween(myId, peerId))
        .fetchOne();

    return count == null ? 0L : count;
  }

  private BooleanExpression cursor(String cursor, UUID idAfter) {
    if (cursor == null || idAfter == null) {
      return null;
    }

    Instant createdAtCursor = Instant.parse(cursor);
    return directMessage.createdAt.lt(createdAtCursor)
        .or(directMessage.createdAt.eq(createdAtCursor).and(directMessage.id.lt(idAfter)));
  }

  private BooleanExpression conversationBetween(UUID myId, UUID peerId) {
    return directMessage.sender.id.eq(myId).and(directMessage.receiver.id.eq(peerId))
        .or(directMessage.sender.id.eq(peerId).and(directMessage.receiver.id.eq(myId)));
  }
}
