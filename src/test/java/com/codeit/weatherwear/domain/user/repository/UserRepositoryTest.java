package com.codeit.weatherwear.domain.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.weatherwear.domain.user.entity.Role;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.global.config.JpaConfig;
import com.codeit.weatherwear.global.request.SortDirection;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import({JpaConfig.class})
class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private TestEntityManager em;

  private final List<User> testUsers = new ArrayList<>();

  @BeforeEach
  void setUp() {
    testUsers.clear();
    for (int i = 1; i <= 5; i++) {
      User user = User.builder()
          .name("user" + i)
          .email("user" + i + "@test.com")
          .password("")
          .role(Role.USER)
          .locked(i % 2 == 0)
          .createdAt(Instant.parse("2025-07-0" + i + "T00:00:00Z"))
          .build();
      testUsers.add(user);
      em.persist(user);
    }
    em.flush();
    em.clear();
  }

  @Test
  void 이메일_오름차순_조회() {
    // when
    Slice<User> result = userRepository.searchUsers(
        null, null, 3, "email", SortDirection.ASCENDING, null, null, null
    );

    // then
    assertThat(result.getContent()).hasSize(3);
    assertThat(result.hasNext()).isTrue();
    assertThat(result.getContent())
        .extracting(User::getEmail)
        .containsExactly("user1@test.com", "user2@test.com", "user3@test.com");
  }

  @Test
  void 이메일_내림차순_조회() {
    // when
    Slice<User> result = userRepository.searchUsers(
        null, null, 3, "email", SortDirection.DESCENDING, null, null, null
    );

    // then
    assertThat(result.getContent()).hasSize(3);
    assertThat(result.hasNext()).isTrue();
    assertThat(result.getContent())
        .extracting(User::getEmail)
        .containsExactly("user5@test.com", "user4@test.com", "user3@test.com");
  }

  @Test
  void createdAt_오름차순_조회() {
    // when
    Slice<User> result = userRepository.searchUsers(
        null, null, 3, "createdAt", SortDirection.ASCENDING, null, null, null
    );

    // then
    assertThat(result.getContent()).hasSize(3);
    assertThat(result.hasNext()).isTrue();
    assertThat(result.getContent())
        .extracting(User::getCreatedAt)
        .isSortedAccordingTo(Comparator.naturalOrder());
  }

  @Test
  void 이메일_기준_커서페이징_오름차순() {
    // given
    String cursor = "user3@test.com";
    UUID idAfter = testUsers.get(2).getId();

    // when
    Slice<User> result = userRepository.searchUsers(
        cursor,
        idAfter,
        2, // limit
        "email",
        SortDirection.ASCENDING,
        null, // emailLike
        null, // roleEqual
        null  // locked
    );

    // then
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.hasNext()).isFalse(); // 총 유저는 5명이고 마지막 페이지니까 false
    assertThat(result.getContent())
        .extracting(User::getEmail)
        .containsExactly("user4@test.com", "user5@test.com");
  }

  @Test
  void createdAt_커서페이징() {
    // given
    User user3 = userRepository.findByEmail("user3@test.com")
        .orElseThrow();
    // when
    Slice<User> result = userRepository.searchUsers(
        user3.getCreatedAt().toString(), user3.getId(), 2,
        "createdAt", SortDirection.ASCENDING, null, null, null
    );

    // then
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getContent())
        .extracting(User::getEmail)
        .containsExactly("user4@test.com", "user5@test.com");
  }

  @Test
  void 조건에_맞는_유저_총_개수() {
    // when
    Long count = userRepository.getTotalCount("user", Role.USER, false);

    // then
    assertThat(count).isEqualTo(3); // locked=false인 USER는 user1, user3, user5
  }


}