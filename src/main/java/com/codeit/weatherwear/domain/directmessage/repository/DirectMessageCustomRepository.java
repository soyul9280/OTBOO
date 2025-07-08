package com.codeit.weatherwear.domain.directmessage.repository;

import com.codeit.weatherwear.domain.directmessage.DirectMessageDto;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface DirectMessageCustomRepository {

  Slice<DirectMessageDto> findDirectMessages(
      UUID myId, UUID peerId, String cursor, UUID idAfter, Pageable pageable);

  long getTotalCount(UUID myId, UUID peerId);
}
