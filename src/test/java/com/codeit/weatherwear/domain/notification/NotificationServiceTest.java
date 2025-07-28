package com.codeit.weatherwear.domain.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.codeit.weatherwear.domain.notification.dto.NotificationDto;
import com.codeit.weatherwear.global.event.DomainEventPublisher;
import com.codeit.weatherwear.global.event.dto.NotificationCreatedEvent;
import com.codeit.weatherwear.domain.notification.Notification.Level;
import com.codeit.weatherwear.domain.notification.repository.NotificationRepository;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.exception.UserNotFoundException;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import com.codeit.weatherwear.global.exception.ErrorCode;
import java.time.Instant;
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
class NotificationServiceTest {

  @Mock
  NotificationRepository notificationRepository;

  @Mock
  UserRepository userRepository;

  @Mock
  DomainEventPublisher eventPublisher;

  @InjectMocks
  NotificationService notificationService;

  User alice;
  User bob;

  String title = "test";
  String content = "test1234";

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
  @DisplayName("생성 성공")
  void create() {
    given(userRepository.existsById(alice.getId())).willReturn(true);
    given(notificationRepository.save(any())).willAnswer(invocation -> {
      Notification notification = invocation.getArgument(0);
      ReflectionTestUtils.setField(notification, "id", UUID.randomUUID());
      ReflectionTestUtils.setField(notification, "createdAt", Instant.now());
      return notification;
    });

    NotificationDto notification = notificationService
        .create(alice.getId(), title, content, Level.INFO);

    assertThat(notification.receiverId()).isEqualTo(alice.getId());
    assertThat(notification.title()).isEqualTo(title);
    assertThat(notification.content()).isEqualTo(content);
    assertThat(notification.level()).isEqualTo(Level.INFO);

    then(userRepository).should().existsById(alice.getId());
    then(notificationRepository).should().save(any());
    then(eventPublisher).should().publish(any(NotificationCreatedEvent.class));
  }

  @Test
  @DisplayName("유저가 존재하지 않으면 알림 생성 실패")
  void noUser() {
    given(userRepository.existsById(alice.getId())).willReturn(false);

    assertThatThrownBy(() -> notificationService.create(alice.getId(), title, content, Level.INFO))
        .hasMessage(ErrorCode.USER_NOT_FOUND.getMessage())
        .isInstanceOf(UserNotFoundException.class);

    then(userRepository).should().existsById(alice.getId());
    then(notificationRepository).shouldHaveNoInteractions();
  }
}