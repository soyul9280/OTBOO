package com.codeit.weatherwear.domain.feed.mapper;

import com.codeit.weatherwear.domain.feed.entity.Feed;
import com.codeit.weatherwear.domain.feed.entity.FeedLike;
import com.codeit.weatherwear.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedLikeMapper {

  public FeedLike toEntity(Feed feed, User user) {
    return FeedLike.builder()
        .feed(feed)
        .user(user)
        .build();
  }

}
