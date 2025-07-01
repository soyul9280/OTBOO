package com.codeit.weatherwear.domain.notification;

import static org.assertj.core.api.Assertions.*;

import com.codeit.weatherwear.domain.notification.Notification.Level;
import com.codeit.weatherwear.domain.notification.repository.NotificationRepository;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import com.codeit.weatherwear.global.config.JpaConfig;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
@Import(JpaConfig.class)
class NotificationRepositoryTest {

  @Autowired
  NotificationRepository notificationRepository;

  @Autowired
  UserRepository userRepository;

  User alice;
  User bob;

  Notification notification1;
  Notification notification2;
  Notification notification3;
  Notification notification4;
  Notification notification5;

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

    userRepository.save(alice);
    userRepository.save(bob);

    notification1 = Notification
        .create(alice.getId(), "피드 작성", "bob이 피드를 작성했습니다.", Level.INFO);
    notification2 = Notification
        .create(bob.getId(), "의상 속성 변경", "내 의상의 속성이 변경되었습니다.", Level.INFO);
    notification3 = Notification
        .create(alice.getId(), "의상 속성 추가", "내 의상에 속성이 추가되었습니다.", Level.INFO);
    notification4 = Notification
        .create(alice.getId(), "피드 작성", "charlie가 피드를 작성했습니다.", Level.INFO);
    notification5 = Notification
        .create(alice.getId(), "의상 속성 변경", "내 의상의 속성이 변경되었습니다.", Level.INFO);


    notificationRepository.save(notification1);
    Thread.sleep(1);
    notificationRepository.save(notification2);
    Thread.sleep(1);
    notificationRepository.save(notification3);
    Thread.sleep(1);
    notificationRepository.save(notification4);
    Thread.sleep(1);
    notificationRepository.save(notification5);
  }

  @Test
  @DisplayName("알림 조회")
  void find() {
    int limit = 20;
    List<NotificationDto> notifications = notificationRepository
        .findNotification(alice.getId(), null, null, limit);

    assertThat(notifications)
        .hasSize(4)
        .allSatisfy(notification -> assertThat(notification.receiverId()).isEqualTo(alice.getId()))
        .satisfiesExactly(
            notification -> assertThat(notification.id()).isEqualTo(notification5.getId()),
            notification -> assertThat(notification.id()).isEqualTo(notification4.getId()),
            notification -> assertThat(notification.id()).isEqualTo(notification3.getId()),
            notification -> assertThat(notification.id()).isEqualTo(notification1.getId())
        );
  }

  @Test
  @DisplayName("알림 조회 - hasNext true인 경우")
  void hasNext() {
    int limit = 2;
    List<NotificationDto> notifications = notificationRepository
        .findNotification(alice.getId(), null, null, limit);

    assertThat(notifications)
        .hasSize(3)
        .allSatisfy(notification -> assertThat(notification.receiverId()).isEqualTo(alice.getId()))
        .satisfiesExactly(
            notification -> assertThat(notification.id()).isEqualTo(notification5.getId()),
            notification -> assertThat(notification.id()).isEqualTo(notification4.getId())
        );
  }

  @Test
  @DisplayName("알림 조회 - 커서가 있는 경우")
  void findWithCursor() {
    int limit = 2;
    Instant cursor = notification4.getCreatedAt().truncatedTo(ChronoUnit.MICROS);
    List<NotificationDto> notifications = notificationRepository
        .findNotification(alice.getId(), cursor.toString(), notification4.getId(), limit);

    assertThat(notifications)
        .hasSize(2)
        .allSatisfy(notification -> assertThat(notification.receiverId()).isEqualTo(alice.getId()))
        .satisfiesExactly(
            notification -> assertThat(notification.id()).isEqualTo(notification3.getId()),
            notification -> assertThat(notification.id()).isEqualTo(notification1.getId())
        );
  }
}