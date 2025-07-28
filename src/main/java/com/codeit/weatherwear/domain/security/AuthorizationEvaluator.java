package com.codeit.weatherwear.domain.security;

import com.codeit.weatherwear.domain.clothes.repository.ClothRepository;
import com.codeit.weatherwear.domain.feed.repository.FeedRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthorizationEvaluator {

  private final ClothRepository clothRepository;
  private final FeedRepository feedRepository;

  // 옷을 등록한 사용자인가?
  public boolean isClothOwner(UUID userId, UUID clothId) {
    return clothRepository.findById(clothId)
        .map(cloth -> cloth.getUser().getId().equals(userId))
        .orElse(false);
  }

  // 피드를 작성한 사용자인가?
  public boolean isFeedAuthor(UUID userId, UUID feedId) {
    return feedRepository.findById(feedId)
        .map(feed -> feed.getAuthor().getId().equals(userId))
        .orElse(false);
  }
}
