package com.codeit.weatherwear.domain.feed.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.weatherwear.domain.feed.dto.condition.FeedSearchCondition;
import com.codeit.weatherwear.domain.feed.entity.Feed;
import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.weather.entity.Humidity;
import com.codeit.weatherwear.domain.weather.entity.Precipitation;
import com.codeit.weatherwear.domain.weather.entity.PrecipitationsType;
import com.codeit.weatherwear.domain.weather.entity.SkyStatus;
import com.codeit.weatherwear.domain.weather.entity.Temperature;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import com.codeit.weatherwear.domain.weather.entity.WindSpeed;
import com.codeit.weatherwear.global.config.JpaConfig;
import com.codeit.weatherwear.global.request.SortDirection;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@ActiveProfiles("test")
@Import({JpaConfig.class})
class FeedRepositoryTest {

  @Autowired
  private TestEntityManager testEntityManager;

  @Autowired
  private EntityManager entityManager;

  @Autowired
  private JPAQueryFactory queryFactory;

  @Autowired
  private FeedRepository feedRepository;

  private Feed feed1, feed2, feed3;

  @BeforeEach
  void setUp() {
    Instant now = Instant.now();
    User user = createUser();
    feed1 = createFeed(user, "Feed1", now.minusSeconds(60), 5);
    feed2 = createFeed(user, "Feed2", now.minusSeconds(30), 10);
    feed3 = createFeed(user, "Feed3", now, 0);
  }

  private User createUser() {
    User user = User.builder()
        .email("test1@test.com")
        .name("test1")
        .password("test1234")
        .build();
    return testEntityManager.persistAndFlush(user);
  }

  private Location createLocation() {
    Location location = new Location(37.513068, 127.102570, 961159, 1953082, "서울 송파구 신천동");
    return testEntityManager.persistAndFlush(location); // 명시적 저장
  }

  private Weather createWeather() {
    Location location = createLocation();
    Weather weather = Weather.builder()
        .forecastAt(Instant.now())
        .forecastedAt(Instant.now())
        .humidity(Humidity.builder().comparedToDayBefore(0.1).current(0.1).build())
        .location(location)
        .precipitation(
            Precipitation.builder().amount(0).probability(0.1).type(PrecipitationsType.NONE)
                .build())
        .temperature(
            Temperature.builder().comparedToDayBefore(10).current(23).max(30).min(20).build())
        .skyStatus(SkyStatus.CLEAR)
        .windSpeed(WindSpeed.builder().speed(10).build())
        .build();
    return testEntityManager.persistAndFlush(weather);
  }

  private Feed createFeed(User author, String content, Instant time, int likeCount) {
    Weather weather = createWeather();
    Feed feed = Feed.builder()
        .author(author)
        .content(content)
        .commentCount(0)
        .likeCount(likeCount)
        .weather(weather)
        .build();
    ReflectionTestUtils.setField(feed, "createdAt", time);

    return testEntityManager.persistAndFlush(feed);
  }

  private FeedSearchCondition createCondition(UUID idAfter, int limit, String sortBy,
      SortDirection sortDirection) {
    return FeedSearchCondition.builder()
        .idAfter(idAfter)
        .limit(limit)
        .sortBy(sortBy)
        .sortDirection(sortDirection)
        .build();
  }

  @Test
  @DisplayName("createdAt 기준 오름차순 정렬 및 페이지네이션이 제대로 동작한다")
  void pagination_with_createdAt_asc() {
    // when & then
    List<Feed> page1 = feedRepository.searchFeeds(
        createCondition(null, 1, "createdAt", SortDirection.ASCENDING));
    assertThat(page1).hasSize(1);
    assertThat(page1.get(0).getId()).isEqualTo(feed1.getId());

    List<Feed> page2 = feedRepository.searchFeeds(
        createCondition(page1.get(0).getId(), 1, "createdAt", SortDirection.ASCENDING));
    assertThat(page2).hasSize(1);
    assertThat(page2.get(0).getId()).isEqualTo(feed2.getId());

    List<Feed> page3 = feedRepository.searchFeeds(
        createCondition(page2.get(0).getId(), 1, "createdAt", SortDirection.ASCENDING));
    assertThat(page3).hasSize(1);
    assertThat(page3.get(0).getId()).isEqualTo(feed3.getId());

    List<Feed> page4 = feedRepository.searchFeeds(
        createCondition(page3.get(0).getId(), 1, "createdAt", SortDirection.ASCENDING));
    assertThat(page4).isEmpty();
  }

  @Test
  @DisplayName("createdAt 기준 내림차순 정렬 및 페이지네이션이 제대로 동작한다")
  void pagination_with_createdAt_desc() {
    // when & then
    List<Feed> page1 = feedRepository.searchFeeds(
        createCondition(null, 1, "createdAt", SortDirection.DESCENDING));
    assertThat(page1).hasSize(1);
    assertThat(page1.get(0).getId()).isEqualTo(feed3.getId());

    List<Feed> page2 = feedRepository.searchFeeds(
        createCondition(page1.get(0).getId(), 1, "createdAt", SortDirection.DESCENDING));
    assertThat(page2).hasSize(1);
    assertThat(page2.get(0).getId()).isEqualTo(feed2.getId());

    List<Feed> page3 = feedRepository.searchFeeds(
        createCondition(page2.get(0).getId(), 1, "createdAt", SortDirection.DESCENDING));
    assertThat(page3).hasSize(1);
    assertThat(page3.get(0).getId()).isEqualTo(feed1.getId());

    List<Feed> page4 = feedRepository.searchFeeds(
        createCondition(page3.get(0).getId(), 1, "createdAt", SortDirection.DESCENDING));
    assertThat(page4).isEmpty();
  }

  @Test
  @DisplayName("likeCount 기준 오름차순 정렬 및 페이지네이션이 제대로 동작한다")
  void pagination_with_likeCount_asc() {
    // when & then
    List<Feed> page1 = feedRepository.searchFeeds(
        createCondition(null, 1, "likeCount", SortDirection.ASCENDING));
    assertThat(page1).hasSize(1);
    assertThat(page1.get(0).getId()).isEqualTo(feed3.getId());
    assertThat(page1.get(0).getLikeCount()).isEqualTo(feed3.getLikeCount());

    List<Feed> page2 = feedRepository.searchFeeds(
        createCondition(page1.get(0).getId(), 1, "likeCount", SortDirection.ASCENDING));
    assertThat(page2).hasSize(1);
    assertThat(page2.get(0).getId()).isEqualTo(feed1.getId());
    assertThat(page2.get(0).getLikeCount()).isEqualTo(feed1.getLikeCount());

    List<Feed> page3 = feedRepository.searchFeeds(
        createCondition(page2.get(0).getId(), 1, "likeCount", SortDirection.ASCENDING));
    assertThat(page3).hasSize(1);
    assertThat(page3.get(0).getId()).isEqualTo(feed2.getId());
    assertThat(page3.get(0).getLikeCount()).isEqualTo(feed2.getLikeCount());

    List<Feed> page4 = feedRepository.searchFeeds(
        createCondition(page3.get(0).getId(), 1, "likeCount", SortDirection.ASCENDING));
    assertThat(page4).isEmpty();
  }

  @Test
  @DisplayName("likeCount 기준 내림차순 정렬 및 페이지네이션이 제대로 동작한다")
  void pagination_with_likeCount_desc() {
    // when & then
    List<Feed> page1 = feedRepository.searchFeeds(
        createCondition(null, 1, "likeCount", SortDirection.DESCENDING));
    assertThat(page1).hasSize(1);
    assertThat(page1.get(0).getId()).isEqualTo(feed2.getId());
    assertThat(page1.get(0).getLikeCount()).isEqualTo(feed2.getLikeCount());

    List<Feed> page2 = feedRepository.searchFeeds(
        createCondition(page1.get(0).getId(), 1, "likeCount", SortDirection.DESCENDING));
    assertThat(page2).hasSize(1);
    assertThat(page2.get(0).getId()).isEqualTo(feed1.getId());
    assertThat(page2.get(0).getLikeCount()).isEqualTo(feed1.getLikeCount());

    List<Feed> page3 = feedRepository.searchFeeds(
        createCondition(page2.get(0).getId(), 1, "likeCount", SortDirection.DESCENDING));
    assertThat(page3).hasSize(1);
    assertThat(page3.get(0).getId()).isEqualTo(feed3.getId());
    assertThat(page3.get(0).getLikeCount()).isEqualTo(feed3.getLikeCount());

    List<Feed> page4 = feedRepository.searchFeeds(
        createCondition(page3.get(0).getId(), 1, "likeCount", SortDirection.DESCENDING));
    assertThat(page4).isEmpty();
  }

}