package com.codeit.weatherwear.domain.weather.batch.task;

import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import com.codeit.weatherwear.domain.weather.service.WeatherFetchService;
import com.codeit.weatherwear.global.event.dto.WeatherAlertEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Processor 정의
 * <p>
 * Reader에서 읽어들인 item(Chunk)을 통해 필요한 로직 수행
 */
@Component
@RequiredArgsConstructor
public class WeatherItemProcessor {

  private final WeatherFetchService weatherFetchService;
  private final UserRepository userRepository;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final WeatherAlertAnalyzer weatherAlertAnalyzer;

  /**
   * 각 Location에 대해 외부 API를 호출해 해당 위치의 날씨 정보를 가져옴
   * <p>
   * Reader에서 받은 Location → List<Weather>로 변환
   * <p>
   * "특별한 변화(비 예보/일교차 심함 등)"가 있다면 알림 이벤트를 발행
   *
   * @return ItemProcessor<Location, List < Weather>>
   */
  @Bean
  public ItemProcessor<Location, List<Weather>> locationWeatherProcessor() {
    return location -> {
      // 해당 위치의 날씨 예보 가져오기
      List<Weather> forecastList = weatherFetchService.fetchWeather(
          location.getLatitude(), location.getLongitude()
      );

      // 오늘 날씨 예보 가져오기
      LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
      Optional<Weather> todayForecastOpt = forecastList.stream()
          .filter(weather -> {
            LocalDate forecastDate = weather.getForecastAt()
                .atZone(ZoneId.of("Asia/Seoul"))
                .toLocalDate();
            return forecastDate.equals(today);
          })
          .findFirst();

      // 오늘 날씨 예보가 존재하는지 확인
      todayForecastOpt.ifPresent(todayForecast -> {
        WeatherAlertResult alertResult = weatherAlertAnalyzer.analyze(todayForecast);
        // 특이 사항 알람이 필요한 지 확인
        if (alertResult.alertNeeded()) {
          List<UUID> receiverIds = userRepository.findUserIdsByLocation(location);
          // 만약 보낼 사람이 없어도 이벤트 발행하지 않음
          if (!receiverIds.isEmpty()) {
            String combinedReason = alertResult.reasons().stream()
                .map(WeatherAlertReason::getCause)
                .collect(Collectors.joining(", "));
            // 필요한 알람에 대해 이벤트 발행
            applicationEventPublisher.publishEvent(
                new WeatherAlertEvent(receiverIds, location.getName(),
                    combinedReason));
          }
        }
      });

      return forecastList;
    };
  }
}
