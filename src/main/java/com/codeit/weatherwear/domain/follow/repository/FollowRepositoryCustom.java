package com.codeit.weatherwear.domain.follow.repository;

import com.codeit.weatherwear.domain.follow.dto.FollowDto;
import com.codeit.weatherwear.domain.follow.dto.FollowSummaryDto;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface FollowRepositoryCustom {
  FollowSummaryDto getSummary(UUID userId, UUID myId);

  Slice<FollowDto> getFollowings(UUID followerId, String cursor, UUID idAfter, String nameLike, Pageable pageable);
  Slice<FollowDto> getFollowers(UUID followeeId, String cursor, UUID idAfter, String nameLike, Pageable pageable);
}
