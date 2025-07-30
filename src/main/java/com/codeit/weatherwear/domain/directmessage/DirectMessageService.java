package com.codeit.weatherwear.domain.directmessage;

import com.codeit.weatherwear.domain.directmessage.dto.DirectMessageDto;
import com.codeit.weatherwear.domain.directmessage.dto.request.DirectMessageCreateRequest;
import com.codeit.weatherwear.domain.directmessage.dto.request.DirectMessageSearchRequest;
import com.codeit.weatherwear.domain.directmessage.repository.DirectMessageRepository;
import com.codeit.weatherwear.global.event.DomainEventPublisher;
import com.codeit.weatherwear.global.event.dto.DirectMessageReceivedEvent;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.exception.UserNotFoundException;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import com.codeit.weatherwear.global.request.SortDirection;
import com.codeit.weatherwear.global.response.PageResponse;
import com.codeit.weatherwear.global.storage.ThumbnailImageStorage;
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
  private final UserRepository userRepository;
  private final DomainEventPublisher eventPublisher;
  private final ThumbnailImageStorage thumbnailImageStorage;

  @Transactional
  public DirectMessageDto create(DirectMessageCreateRequest request) {
    User receiver = userRepository.findById(request.receiverId())
        .orElseThrow(UserNotFoundException::new);
    User sender = userRepository.findById(request.senderId())
        .orElseThrow(UserNotFoundException::new);
    String content = request.content();

    DirectMessage directMessage = directMessageRepository
        .save(DirectMessage.create(sender, receiver, content));

    log.info("direct message created. id={}", directMessage.getId());

    String senderProfileImage = null;
    String receiverProfileImage = null;

    if (sender.getProfileImageUrl() != null) {
      senderProfileImage = thumbnailImageStorage.get(sender.getProfileImageUrl());
    }
    if (receiver.getProfileImageUrl() != null) {
      receiverProfileImage = thumbnailImageStorage.get(receiver.getProfileImageUrl());
    }

    DirectMessageDto dto = DirectMessageDto
        .from(directMessage, senderProfileImage, receiverProfileImage);

    eventPublisher.publish(new DirectMessageReceivedEvent(dto));
    return dto;
  }

  public PageResponse<DirectMessageDto> findDirectMessages(
      UUID myId, DirectMessageSearchRequest request, Pageable pageable
  ) {
    Slice<DirectMessageDto> directMessages = directMessageRepository
        .findDirectMessages(myId, request.userId(), request.cursor(), request.idAfter(), pageable);
    long totalCount = directMessageRepository.getTotalCount(myId, request.userId());
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
