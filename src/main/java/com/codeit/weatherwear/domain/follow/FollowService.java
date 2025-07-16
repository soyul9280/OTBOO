package com.codeit.weatherwear.domain.follow;

import com.codeit.weatherwear.domain.follow.dto.FollowDto;
import com.codeit.weatherwear.domain.follow.dto.FollowSummaryDto;
import com.codeit.weatherwear.domain.follow.dto.request.FollowCreateRequest;
import com.codeit.weatherwear.domain.follow.dto.request.FollowerRequest;
import com.codeit.weatherwear.domain.follow.dto.request.FollowingRequest;
import com.codeit.weatherwear.domain.follow.exception.FollowDuplicatedException;
import com.codeit.weatherwear.domain.follow.repository.FollowRepository;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.exception.UserNotFoundException;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
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
public class FollowService {

  private final FollowRepository followRepository;
  private final UserRepository userRepository;

  @Transactional
  public FollowDto create(FollowCreateRequest request) {
    User follower = userRepository.findById(request.followerId())
        .orElseThrow(UserNotFoundException::new);
    User followee = userRepository.findById(request.followeeId())
        .orElseThrow(UserNotFoundException::new);

    //이미 팔로우 한 유저를 팔로우 할 수 없음
    if (followRepository.existsByFolloweeAndFollower(followee, follower)) {
      throw FollowDuplicatedException.withId(followee.getId(), follower.getId());
    }

    Follow follow = followRepository.save(Follow.create(followee, follower));

    FollowDto dto = FollowDto.from(follow);
    log.info("Follow 생성: {}", dto);

    return dto;
  }

  public FollowSummaryDto getSummary(UUID userId, UUID myId) {
    return followRepository.getSummary(userId, myId);
  }

  public PageResponse<FollowDto> getFollowings(FollowingRequest followingRequest, Pageable pageable) {
    Slice<FollowDto> followings = followRepository
        .getFollowings(followingRequest.followerId(), followingRequest.cursor(),
            followingRequest.idAfter(), followingRequest.nameLike(), pageable);
    long totalCount = followRepository.countByFollower_Id(followingRequest.followerId());

    return toPageResponse(followings, totalCount);
  }

  public PageResponse<FollowDto> getFollowers(FollowerRequest followerRequest, Pageable pageable) {
    Slice<FollowDto> followers = followRepository
        .getFollowers(followerRequest.followeeId(), followerRequest.cursor(),
            followerRequest.idAfter(), followerRequest.nameLike(), pageable);
    long totalCount = followRepository.countByFollowee_Id(followerRequest.followeeId());
    return toPageResponse(followers, totalCount);
  }

  private PageResponse<FollowDto> toPageResponse(Slice<FollowDto> follows, long totalCount) {
    List<FollowDto> content = follows.getContent();
    boolean hasNext = follows.hasNext();
    Instant nextCursor = null;
    UUID nextIdAfter = null;

    if (hasNext) {
      FollowDto followDto = content.get(content.size() - 1);

      nextCursor = followDto.createdAt();
      nextIdAfter = followDto.id();
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

  @Transactional
  public void delete(UUID id) {
    followRepository.findById(id).ifPresent(follow -> {
        followRepository.delete(follow);
        log.info("Follow 삭제. id: {}", id);
    });
  }

}
