package com.codeit.weatherwear.domain.weather.batch.scheduler;

import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
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

  // 매일 3시 10분에 돌아가는 스케줄러
  // -> 예보 데이터를 정시가 아닌 정시 + 10분에 준다고 해서 해당 시각으로 설정
  @Scheduled(cron = "0 10 3 * * ?", zone = "Asia/Seoul")
  public void runWeatherBatchJob() {
    Date date = new Date();
    JobParameters parameters = new JobParametersBuilder()
        .addDate("runTime", date)
        .toJobParameters();

    try {
      log.info(">> Weather Fetch Batch Job Start at {}", date);
      jobLauncher.run(weatherFetchJob, parameters);
    } catch (Exception e) {
      log.error(">> Weather Fetch Batch Job Failed", e);
    }
  }

}
