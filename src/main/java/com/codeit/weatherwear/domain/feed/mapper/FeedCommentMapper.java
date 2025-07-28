package com.codeit.weatherwear.domain.feed.mapper;

import com.codeit.weatherwear.domain.feed.dto.response.FeedCommentDto;
import com.codeit.weatherwear.domain.feed.entity.Feed;
import com.codeit.weatherwear.domain.feed.entity.FeedComment;
import com.codeit.weatherwear.domain.follow.dto.UserSummaryDto;
import com.codeit.weatherwear.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedCommentMapper {

  public FeedComment toEntity(Feed feed, User author, String content) {
    return FeedComment.builder()
        .feed(feed)
        .author(author)
        .content(content)
        .build();
  }

  public FeedCommentDto toDto(FeedComment feedComment, UserSummaryDto author) {
    return FeedCommentDto.builder()
        .id(feedComment.getId())
        .createdAt(feedComment.getCreatedAt())
        .feedId(feedComment.getFeed().getId())
        .author(author)
        .content(feedComment.getContent())
        .build();
  }

}
