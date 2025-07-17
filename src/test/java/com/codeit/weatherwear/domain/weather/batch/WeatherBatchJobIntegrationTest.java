package com.codeit.weatherwear.domain.weather.batch;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.location.repository.LocationRepository;
import com.codeit.weatherwear.domain.weather.repository.WeatherRepository;
import com.codeit.weatherwear.domain.weather.service.WeatherFetchService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.Mock;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@SpringBatchTest  // 배치 테스트용 유틸 사용을 위함
@ActiveProfiles("test")
@TestInstance(Lifecycle.PER_CLASS)
public class WeatherBatchJobIntegrationTest {

  @Autowired
  private JobLauncherTestUtils jobLauncherTestUtils;

  @Autowired
  private WeatherRepository weatherRepository;

  @Autowired
  private LocationRepository locationRepository;

  @Autowired
  private Job weatherFetchJob;

  @Mock
  private WeatherFetchService weatherFetchService;

  private double latitude, longitude;
  private int nx, ny;
  private String addrName;
  private Location location;

  @BeforeEach
  void setUp(@Autowired Job weatherFetchJob) {
    // 데이터 독립성 유지를 위함
    weatherRepository.deleteAll();
    locationRepository.deleteAll();

    jobLauncherTestUtils.setJob(weatherFetchJob);

    latitude = 37.5759;
    longitude = 126.9768;
    nx = 60;
    ny = 127;
    addrName = "서울 종로구 세종로";
    location = new Location(latitude, longitude, nx, ny, addrName);
    locationRepository.save(location);
  }

  @AfterEach
  void cleanUp() {
    // 안전장치 (혹시나 BeforeEach 때 처리가 안 될까봐)
    weatherRepository.deleteAll();
    locationRepository.deleteAll();
  }

  @Test
  @DisplayName("날씨 배치 Job이 정상적으로 수행된다")
  void weather_batch_job_execute_successfully() throws Exception {
    // given
    JobParameters jobParameters = new JobParametersBuilder()
        .addLong("runTime", System.currentTimeMillis())
        .toJobParameters();

    // when
    JobExecution execution = jobLauncherTestUtils.launchJob(jobParameters);

    // then
    assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
    assertThat(weatherRepository.findAll()).isNotEmpty();
  }

  @Test
  @DisplayName("날씨 배치 Job에서 예외가 발생하면 Skip 된다")
  void weather_batch_job_execute_skipped() throws Exception {
    // given
    // 잘못된 위치 정보 전달하기
    locationRepository.save(new Location(-1.0, -999.0, 55, 130, "시 군 구"));

    // when
    JobParameters jobParameters = new JobParametersBuilder()
        .addLong("runTime", System.currentTimeMillis())
        .toJobParameters();

    JobExecution execution = jobLauncherTestUtils.launchJob(jobParameters);

    // then
    // JOB/STEP은 정상 COMPLETED로 끝나야 한다 (예외 던졌지만 skip 됨)
    assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

    // 정상 저장된 수
    assertThat(weatherRepository.findAll().size()).isEqualTo(5);
  }
}
