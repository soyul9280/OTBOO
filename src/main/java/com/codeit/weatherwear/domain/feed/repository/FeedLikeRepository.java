package com.codeit.weatherwear.domain.feed.repository;

import com.codeit.weatherwear.domain.feed.entity.Feed;
import com.codeit.weatherwear.domain.feed.entity.FeedLike;
import com.codeit.weatherwear.domain.user.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedLikeRepository extends JpaRepository<FeedLike, UUID> {

  @Query("select fl from FeedLike fl where fl.feed.id=:feedId and fl.user.id=:userId")
  Optional<FeedLike> findFeedLikeByFeedIdAndUserId(@Param("feedId") UUID feedId,
      @Param("userId") UUID userId);

  boolean existsFeedLikeByFeedAndUser(Feed feed, User user);

  void deleteAllByFeed(Feed feed);
}
