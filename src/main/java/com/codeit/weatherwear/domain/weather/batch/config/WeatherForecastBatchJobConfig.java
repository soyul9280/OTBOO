package com.codeit.weatherwear.domain.weather.batch.config;

import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.weather.batch.task.WeatherItemProcessor;
import com.codeit.weatherwear.domain.weather.batch.task.WeatherItemReader;
import com.codeit.weatherwear.domain.weather.batch.task.WeatherItemWriter;
import com.codeit.weatherwear.domain.weather.entity.Weather;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Job/Step/트랜잭션 등 배치 전반적인 설정 담당
 */
@Configuration
@RequiredArgsConstructor
public class WeatherForecastBatchJobConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;

  private final WeatherItemReader weatherItemReader;
  private final WeatherItemProcessor weatherItemProcessor;
  private final WeatherItemWriter weatherItemWriter;

  /**
   * 하나의 Step 정의
   * <p>
   * - Location 단위로 읽고 (Reader)
   * <p>
   * - 각 Location에 대해 날씨를 조회한 후 Weather 형식으로 변환 (Processor)
   * <p>
   * - DB에 일괄 저장 (Writer)
   *
   * @return Step
   */
  @Bean
  public Step weatherFetchStep(WeatherBatchProperties weatherBatchProperties,
      WeatherSkipListener weatherSkipListener) {
    // 한 Step(청크) 마다 트랜잭션 적용
    return new StepBuilder("weatherFetchStep", jobRepository)
        .<Location, List<Weather>>chunk(weatherBatchProperties.getChunkSize(), transactionManager)
        .reader(weatherItemReader.locationReader())
        .processor(weatherItemProcessor.locationWeatherProcessor())
        .writer(weatherItemWriter.weatherWriter())
        .faultTolerant()  // 예외 발생 시 대처
        .skip(Exception.class)  // 해당 예외 타입이 발생하면 SKip
        .skipLimit(10)  // Skip 제한 횟수는 10회 (필요 시 최대 10번 스킵 가능)
        .listener(weatherSkipListener)
        .build();
  }

  /**
   * 전체 배치 Job 정의
   * <p>
   * - weatherFetchStep을 실행
   *
   * @return Job
   */
  @Bean
  public Job weatherFetchJob(Step weatherFetchStep) {
    return new JobBuilder("weatherFetchJob", jobRepository)
        .start(weatherFetchStep)
        .build();
  }
}
