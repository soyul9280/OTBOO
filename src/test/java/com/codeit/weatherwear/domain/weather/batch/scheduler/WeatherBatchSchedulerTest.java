package com.codeit.weatherwear.domain.weather.batch.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith({MockitoExtension.class,
    OutputCaptureExtension.class}) // 로그 검증을 위함 (OutputCaptureExtension.class)
class WeatherBatchSchedulerTest {

  @InjectMocks
  private WeatherBatchScheduler weatherBatchScheduler;

  @Mock
  private JobLauncher jobLauncher;

  @Mock
  private Job weatherFetchJob;

  @Test
  @DisplayName("스케줄러가 정상적으로 JOB을 실행한다")
  void run_weather_batch_job_by_scheduler(CapturedOutput output) throws Exception {
    // given
    JobExecution execution = new JobExecution(1L);
    given(jobLauncher.run(eq(weatherFetchJob), any(JobParameters.class))).willReturn(execution);

    // when
    weatherBatchScheduler.runWeatherBatchJob();

    // then
    verify(jobLauncher).run(eq(weatherFetchJob), any(JobParameters.class));
    assertThat(output.getOut()).contains(">> Weather Fetch Batch Job Start");
  }

  @Test
  @DisplayName("스케줄러 실행 중 예외가 발생해도 로깅 후 끝난다")
  void run_weather_batch_job_occurred_exception_by_scheduler(CapturedOutput output)
      throws Exception {
    // given
    given(jobLauncher.run(any(), any())).willThrow(new RuntimeException("실패 테스트"));

    // when
    weatherBatchScheduler.runWeatherBatchJob();

    // then
    verify(jobLauncher).run(any(), any());

    assertThat(output.getOut()).contains(">> Weather Fetch Batch Job Start");
    assertThat(output.getOut()).contains(">> Weather Fetch Batch Job Failed");

  }

}