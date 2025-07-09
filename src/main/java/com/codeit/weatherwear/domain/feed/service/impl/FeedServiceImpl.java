package com.codeit.weatherwear.domain.feed.service.impl;

import com.codeit.weatherwear.domain.feed.dto.condition.FeedSearchCondition;
import com.codeit.weatherwear.domain.feed.dto.request.FeedCreateRequest;
import com.codeit.weatherwear.domain.feed.dto.request.FeedGetParamRequest;
import com.codeit.weatherwear.domain.feed.dto.request.FeedUpdateRequest;
import com.codeit.weatherwear.domain.feed.dto.response.FeedDto;
import com.codeit.weatherwear.domain.feed.entity.Feed;
import com.codeit.weatherwear.domain.feed.exception.FeedNotFoundException;
import com.codeit.weatherwear.domain.feed.mapper.FeedMapper;
import com.codeit.weatherwear.domain.feed.repository.FeedRepository;
import com.codeit.weatherwear.domain.feed.service.FeedCommentService;
import com.codeit.weatherwear.domain.feed.service.FeedLikeService;
import com.codeit.weatherwear.domain.feed.service.FeedService;
import com.codeit.weatherwear.domain.follow.dto.UserSummaryDto;
import com.codeit.weatherwear.domain.ootd.dto.response.OotdDto;
import com.codeit.weatherwear.domain.ootd.service.OotdService;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.exception.UserNotFoundException;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import com.codeit.weatherwear.domain.weather.dto.response.WeatherSummaryDto;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import com.codeit.weatherwear.domain.weather.mapper.WeatherMapper;
import com.codeit.weatherwear.domain.weather.repository.WeatherRepository;
import com.codeit.weatherwear.global.response.PageResponse;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

  private final UserRepository userRepository;
  private final FeedMapper feedMapper;
  private final FeedRepository feedRepository;
  private final OotdService ootdService;
  private final FeedCommentService feedCommentService;
  private final FeedLikeService feedLikeService;
  private final WeatherMapper weatherMapper;
  private final WeatherRepository weatherRepository;

  @Transactional
  @Override
  public PageResponse<FeedDto> getFeedList(FeedGetParamRequest paramRequest, UUID currentUserId) {
    log.info("Request Get Feed List");

    FeedSearchCondition condition = paramRequest.toSearchCondition();
    Slice<Feed> feedList = feedRepository.searchFeeds(condition);
    long totalCount = feedRepository.getTotalFeedCount();

    return toPageResponse(currentUserId, feedList, condition, totalCount);
  }

  @Transactional
  @Override
  public FeedDto createFeed(FeedCreateRequest feedCreateRequest, UUID currentUserId) {
    log.info("Request Create Feed - authorId: {}", feedCreateRequest.getAuthorId());

    User author = userRepository.findById(feedCreateRequest.getAuthorId())
        .orElseThrow(UserNotFoundException::new);
    Weather weather = weatherRepository.findById(feedCreateRequest.getWeatherId())
        .orElseThrow(RuntimeException::new);

    Feed feed = feedMapper.toEntity(author, weather, feedCreateRequest);
    Feed saved = feedRepository.save(feed);
    List<OotdDto> ootdList = ootdService.createOotdList(feed, feedCreateRequest.getClothesIds());

    return toFeedDto(saved, ootdList, false);
  }

  @Transactional
  @Override
  public FeedDto updateFeed(UUID feedId, FeedUpdateRequest feedUpdateRequest, UUID currentUserId) {
    log.info("Request Update Feed - feedId: {}", feedId);

    Feed feed = feedRepository.findById(feedId)
        .orElseThrow(() -> new FeedNotFoundException(feedId));
    feed.updateContent(feedUpdateRequest.getContent());

    return toFeedDto(feed, currentUserId);
  }

  @Transactional
  @Override
  public void deleteFeed(UUID feedId, UUID currentUserId) {
    log.info("Request Delete Feed - feedId: {}", feedId);

    Feed feed = feedRepository.findById(feedId)
        .orElseThrow(() -> new FeedNotFoundException(feedId));

    feedLikeService.deleteAllFeedLikeInFeed(feed);
    feedCommentService.deleteFeedCommentsByFeed(feed);
    feedRepository.delete(feed);
  }

  // 생성/삭제
  private FeedDto toFeedDto(Feed feed, List<OotdDto> ootds, boolean likedByMe) {
    UserSummaryDto authorDto = UserSummaryDto.from(feed.getAuthor());
    WeatherSummaryDto weatherSummaryDto = weatherMapper.toSummaryDto(feed.getWeather());

    return feedMapper.toDto(feed, authorDto, weatherSummaryDto, ootds, likedByMe);
  }

  // 일반적인 상황 (조회/갱신)
  private FeedDto toFeedDto(Feed feed, UUID currentUserId) {
    UserSummaryDto authorDto = UserSummaryDto.from(feed.getAuthor());
    WeatherSummaryDto weatherSummaryDto = weatherMapper.toSummaryDto(feed.getWeather());
    List<OotdDto> ootds = ootdService.findOotdByFeedId(feed.getId());

    boolean likedByMe = feedLikeService.isLikedByMe(feed, currentUserId);

    return feedMapper.toDto(feed, authorDto, weatherSummaryDto, ootds, likedByMe);
  }

  private PageResponse<FeedDto> toPageResponse(UUID currentUserId, Slice<Feed> feedSlice,
      FeedSearchCondition condition,
      long totalCount) {

    List<Feed> feeds = feedSlice.getContent();

    List<FeedDto> data = feeds.stream().map(feed -> toFeedDto(feed, currentUserId))
        .collect(Collectors.toList());

    UUID nextIdAfter = null;
    Object nextCursor = null;
    if (feedSlice.hasNext() && !data.isEmpty()) {
      Feed nextFirstItem = feeds.get(feeds.size() - 1);
      nextIdAfter = nextFirstItem.getId();
      nextCursor = nextFirstItem.getCreatedAt();
    }

    return new PageResponse<>(
        data,
        nextCursor,
        nextIdAfter,
        feedSlice.hasNext(),
        totalCount,
        condition.getSortBy(),
        condition.getSortDirection().name()
    );
  }

}
