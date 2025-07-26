package com.codeit.weatherwear.domain.weather.batch.scheduler;

import com.codeit.weatherwear.domain.weather.repository.WeatherRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherBatchScheduler {

  private final JobLauncher jobLauncher;
  private final Job weatherFetchJob;
  private final WeatherRepository weatherRepository;

  // 매일 3시 10분에 돌아가는 스케줄러
  // -> 예보 데이터를 정시가 아닌 정시 + 10분에 준다고 해서 해당 시각으로 설정
//  @Scheduled(cron = "0 10 3 * * ?", zone = "Asia/Seoul")
  @Scheduled(cron = "0 22 0 * * ?", zone = "Asia/Seoul")
  public void runWeatherBatchJob() {
    Date date = new Date();
    JobParameters parameters = new JobParametersBuilder()
        .addDate("runTime", date)
        .toJobParameters();

    try {
      log.info(">> Weather Fetch Batch Job Start at {}", date);
      JobExecution execution = jobLauncher.run(weatherFetchJob, parameters);

      if (execution.getStatus() == BatchStatus.COMPLETED) {
        log.info(">> Weather Fetch Batch Job Completed, Starting Delete");
        cleanOldOrphanWeather();
      }

    } catch (Exception e) {
      log.error(">> Weather Fetch Batch Job Failed", e);
    }
  }

  public void cleanOldOrphanWeather() {
    ZoneId zone = ZoneId.of("Asia/Seoul");
    LocalDate today = LocalDate.now(zone);
    Instant cutOffTime = today.atStartOfDay(zone).toInstant();

    int deletedCount = weatherRepository.deleteOldOrphanForecast(cutOffTime);
    log.info(">> Deleted {} old orphan Weather records", deletedCount);
  }

}
