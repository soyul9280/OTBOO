package com.codeit.weatherwear.domain.directmessage;

import com.codeit.weatherwear.domain.directmessage.repository.DirectMessageRepository;
import com.codeit.weatherwear.global.request.SortDirection;
import com.codeit.weatherwear.global.response.PageResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectMessageService {

  private final DirectMessageRepository directMessageRepository;

  public PageResponse<DirectMessageDto> findDirectMessages(UUID myId, UUID peerId,
      String cursor, UUID idAfter, Pageable pageable) {
    Slice<DirectMessageDto> directMessages = directMessageRepository
        .findDirectMessages(myId, peerId, cursor, idAfter, pageable);
    long totalCount = directMessageRepository.getTotalCount(myId, peerId);
    return toPageResponse(directMessages, totalCount);
  }

  private PageResponse<DirectMessageDto> toPageResponse(
      Slice<DirectMessageDto> directMessages, long totalCount
  ) {
    List<DirectMessageDto> content = directMessages.getContent();
    boolean hasNext = directMessages.hasNext();
    Instant nextCursor = null;
    UUID nextIdAfter = null;

    if (hasNext) {
      DirectMessageDto directMessageDto = content.get(content.size() - 1);

      nextCursor = directMessageDto.createdAt();
      nextIdAfter = directMessageDto.id();
    }
    String sortBy = "createdAt";

    return new PageResponse<>(
        content,
        nextCursor,
        nextIdAfter,
        hasNext,
        totalCount,
        sortBy,
        SortDirection.DESCENDING.name()
    );
  }
}
