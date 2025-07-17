package com.codeit.weatherwear.domain.follow;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.weatherwear.domain.follow.dto.FollowDto;
import com.codeit.weatherwear.domain.follow.dto.FollowSummaryDto;
import com.codeit.weatherwear.domain.follow.repository.FollowRepository;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import com.codeit.weatherwear.global.config.JpaConfig;
import com.codeit.weatherwear.global.config.TestContainerConfig;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@Import({JpaConfig.class, TestContainerConfig.class})
class FollowRepositoryTest {

  @Autowired
  FollowRepository followRepository;
  @Autowired
  UserRepository userRepository;

  @Autowired
  EntityManager em;

  UUID followingMeId;

  User alice;
  User bob;
  User charlie;

  Follow aliceFollowBob;
  Follow charlieFollowBob;

  @BeforeEach
  void setUp() throws InterruptedException {
    alice = User.builder()
        .email("alice@test.com")
        .name("alice")
        .password("alice1234")
        .build();

    bob = User.builder()
        .email("bob@test.com")
        .name("bob")
        .password("bob1234")
        .build();

    charlie = User.builder()
        .email("c@c.com")
        .name("charlie")
        .password("charlie1234")
        .build();

    userRepository.save(alice);
    userRepository.save(bob);
    userRepository.save(charlie);

    aliceFollowBob = followRepository.save(Follow.create(bob, alice));
    Thread.sleep(1);
    charlieFollowBob = followRepository.save(Follow.create(bob, charlie));
    em.flush();
    em.clear();

    followingMeId = aliceFollowBob.getId();
  }

  @Test
  @DisplayName("follow summary 조회")
  void getSummary() {
    FollowSummaryDto summary = followRepository.getSummary(bob.getId(), alice.getId());

    assertThat(summary.followeeId()).isEqualTo(bob.getId());
    assertThat(summary.followerCount()).isEqualTo(2);
    assertThat(summary.followingCount()).isEqualTo(0);
    assertThat(summary.followedByMe()).isTrue();
    assertThat(summary.followedByMeId()).isEqualTo(followingMeId);
    assertThat(summary.followingMe()).isFalse();
  }

  @Test
  @DisplayName("following 목록 조회 - 전체 조회")
  void getFollowing() {
    int limit = 20;
    Slice<FollowDto> followings = followRepository
        .getFollowings(alice.getId(), null, null, null, PageRequest.of(0, limit));
    List<FollowDto> content = followings.getContent();

    assertThat(followings.hasNext()).isFalse();
    assertThat(content)
        .hasSize(1)
        .satisfiesExactly(followDto -> {
          assertThat(followDto.follower().userId()).isEqualTo(alice.getId());
          assertThat(followDto.followee().userId()).isEqualTo(bob.getId());
        });
  }

  @Test
  @DisplayName("following 목록 조회- cursor")
  void getFollowingWithCursor() {
    int limit = 1;
    Instant cursor = aliceFollowBob.getCreatedAt().truncatedTo(ChronoUnit.MICROS);
    Slice<FollowDto> followings = followRepository
        .getFollowings(alice.getId(), cursor.toString(), aliceFollowBob.getId(), null, PageRequest.of(0, limit));
    List<FollowDto> content = followings.getContent();

    assertThat(followings.hasNext()).isFalse();
    assertThat(content).hasSize(0);
  }

  @Test
  @DisplayName("following 목록 조회- name")
  void getFollowingWithName() {
    int limit = 20;
    Slice<FollowDto> followings = followRepository
        .getFollowings(alice.getId(), null, null, "bob", PageRequest.of(0, limit));
    List<FollowDto> content = followings.getContent();

    assertThat(followings.hasNext()).isFalse();
    assertThat(content)
        .hasSize(1)
        .allSatisfy(following -> assertThat(following.followee().userId()).isEqualTo(bob.getId()))
        .satisfiesExactly(
            follower1 -> assertThat(follower1.follower().userId()).isEqualTo(alice.getId())
        );
  }

  @Test
  @DisplayName("follower 목록 조회 - 전체 조회")
  void getAllFollower() {
    int limit = 20;
    Slice<FollowDto> followers = followRepository
        .getFollowers(bob.getId(), null, null, null, PageRequest.of(0, limit));
    List<FollowDto> content = followers.getContent();

    assertThat(followers.hasNext()).isFalse();
    assertThat(content)
        .hasSize(2)
        .allSatisfy(follower -> assertThat(follower.followee().userId()).isEqualTo(bob.getId()))
        .satisfiesExactly(
            follower1 -> assertThat(follower1.follower().userId()).isEqualTo(charlie.getId()),
            follower2 -> assertThat(follower2.follower().userId()).isEqualTo(alice.getId())
        );
  }

  @Test
  @DisplayName("follower 목록 조회- cursor")
  void getFollowerWithCursor() {
    int limit = 1;
    Instant cursor = charlieFollowBob.getCreatedAt().truncatedTo(ChronoUnit.MICROS);
    Slice<FollowDto> followers = followRepository
        .getFollowers(bob.getId(), cursor.toString(), charlieFollowBob.getId(), null, PageRequest.of(0, limit));
    List<FollowDto> content = followers.getContent();

    assertThat(followers.hasNext()).isFalse();
    assertThat(content)
        .hasSize(1)
        .allSatisfy(follower -> assertThat(follower.followee().userId()).isEqualTo(bob.getId()))
        .satisfiesExactly(
            follower1 -> assertThat(follower1.follower().userId()).isEqualTo(alice.getId())
        );
  }

  @Test
  @DisplayName("follower 목록 조회- name")
  void getFollowerWithName() {
    int limit = 20;
    Slice<FollowDto> followers = followRepository
        .getFollowers(bob.getId(), null, null, "alice", PageRequest.of(0, limit));
    List<FollowDto> content = followers.getContent();

    assertThat(followers.hasNext()).isFalse();
    assertThat(content)
        .hasSize(1)
        .allSatisfy(follower -> assertThat(follower.followee().userId()).isEqualTo(bob.getId()))
        .satisfiesExactly(
            follower1 -> assertThat(follower1.follower().userId()).isEqualTo(alice.getId())
        );
  }
}