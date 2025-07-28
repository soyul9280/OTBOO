package com.codeit.weatherwear.domain.follow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.codeit.weatherwear.domain.follow.dto.FollowDto;
import com.codeit.weatherwear.domain.follow.dto.request.FollowCreateRequest;
import com.codeit.weatherwear.domain.follow.exception.FollowDuplicatedException;
import com.codeit.weatherwear.domain.follow.exception.SelfFollowNotAllowedException;
import com.codeit.weatherwear.domain.follow.repository.FollowRepository;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import com.codeit.weatherwear.global.event.DomainEventPublisher;
import com.codeit.weatherwear.global.event.dto.NewFollowerEvent;
import com.codeit.weatherwear.global.exception.ErrorCode;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

  @Mock
  FollowRepository followRepository;
  @Mock
  UserRepository userRepository;
  @Mock
  DomainEventPublisher eventPublisher;

  @InjectMocks
  FollowService followService;

  User alice;
  User bob;

  @BeforeEach
  void setUp() {
    alice = User.builder()
        .email("alice@aaa.com")
        .name("alice")
        .password("alice123")
        .build();
    bob = User.builder()
        .email("bob@bbb.com")
        .name("bob")
        .password("bob123")
        .build();

    ReflectionTestUtils.setField(alice, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(bob, "id", UUID.randomUUID());
  }

  @Test
  @DisplayName("성공")
  void createFollow() {
    FollowCreateRequest request = new FollowCreateRequest(bob.getId(), alice.getId());

    given(userRepository.findById(request.followerId())).willReturn(Optional.of(alice));
    given(userRepository.findById(request.followeeId())).willReturn(Optional.of(bob));
    given(followRepository.existsByFolloweeAndFollower(bob, alice)).willReturn(false);

    given(followRepository.save(any(Follow.class))).willAnswer(i -> {
      Follow follow = i.getArgument(0);
      ReflectionTestUtils.setField(follow, "id", UUID.randomUUID());
      ReflectionTestUtils.setField(follow, "createdAt", Instant.now());
      return follow;
    });

    FollowDto followDto = followService.create(request);

    assertThat(followDto.follower().userId()).isEqualTo(alice.getId());
    assertThat(followDto.follower().name()).isEqualTo(alice.getName());
    assertThat(followDto.followee().userId()).isEqualTo(bob.getId());
    assertThat(followDto.followee().name()).isEqualTo(bob.getName());

    then(userRepository).should().findById(request.followerId());
    then(userRepository).should().findById(request.followeeId());
    then(followRepository).should().existsByFolloweeAndFollower(bob, alice);
    then(eventPublisher).should().publish(any(NewFollowerEvent.class));

    then(followRepository).should().save(any());
  }

  @Test
  @DisplayName("이미 팔로우 한 유저를 팔로우 할 수 없음")
  void followDuplication() {
    FollowCreateRequest request = new FollowCreateRequest(bob.getId(), alice.getId());

    given(userRepository.findById(request.followerId())).willReturn(Optional.of(alice));
    given(userRepository.findById(request.followeeId())).willReturn(Optional.of(bob));
    given(followRepository.existsByFolloweeAndFollower(bob, alice)).willReturn(true);

    assertThatThrownBy(() -> followService.create(request))
        .isInstanceOf(FollowDuplicatedException.class)
        .hasMessage(ErrorCode.FOLLOW_DUPLICATED.getMessage());

    then(userRepository).should().findById(request.followerId());
    then(userRepository).should().findById(request.followeeId());
    then(followRepository).should().existsByFolloweeAndFollower(bob, alice);

    then(followRepository).shouldHaveNoMoreInteractions();
    then(followRepository).should(never()).save(any());
    then(userRepository).shouldHaveNoMoreInteractions();
  }

  @Test
  @DisplayName("자기 자신을 팔로우 할 수 없음")
  void selfFollowing() {
    FollowCreateRequest request = new FollowCreateRequest(alice.getId(), alice.getId());

    given(userRepository.findById(request.followerId())).willReturn(Optional.of(alice));
    given(followRepository.existsByFolloweeAndFollower(alice, alice)).willReturn(false);

    assertThatThrownBy(() -> followService.create(request))
        .isInstanceOf(SelfFollowNotAllowedException.class)
        .hasMessage(ErrorCode.SELF_FOLLOW_NOT_ALLOWED.getMessage());

    then(userRepository).should(times(2)).findById(request.followerId());
    then(followRepository).should().existsByFolloweeAndFollower(alice, alice);

    then(followRepository).shouldHaveNoMoreInteractions();
    then(followRepository).should(never()).save(any());
    then(userRepository).shouldHaveNoMoreInteractions();
  }

}