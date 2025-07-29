package com.codeit.weatherwear.domain.follow.repository;

import com.codeit.weatherwear.domain.follow.Follow;
import com.codeit.weatherwear.domain.user.entity.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

public interface FollowRepository extends
    JpaRepository<Follow, UUID>, FollowRepositoryCustom, QuerydslPredicateExecutor<Follow> {

  long countByFollower_Id(UUID followerId);

  long countByFollowee_Id(UUID followeeId);

  boolean existsByFolloweeAndFollower(User followee, User follower);

  @Query("SELECT DISTINCT f.follower.id FROM Follow f WHERE f.followee.id = :followeeId")
  List<UUID> findFollowerIdsByFolloweeId(@Param("followeeId") UUID followeeId);
}
