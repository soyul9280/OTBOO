package com.codeit.weatherwear.domain.weather.batch;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.weatherwear.domain.location.entity.Location;
import com.codeit.weatherwear.domain.location.repository.LocationRepository;
import com.codeit.weatherwear.domain.weather.repository.WeatherRepository;
import com.codeit.weatherwear.domain.weather.service.WeatherFetchService;
import com.codeit.weatherwear.global.config.TestContainerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.Mock;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@SpringBatchTest  // 배치 테스트용 유틸 사용을 위함
@ActiveProfiles({"test", "reader-exception"})
@TestInstance(Lifecycle.PER_CLASS)
@Import({FailingReaderConfig.class, TestContainerConfig.class})
public class BatchJobReaderExceptionTest {

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
  void setUp() {
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
  @DisplayName("Reader에서 예외가 발생하여 FAILED로 종료된다")
  void weather_batch_job_reader_error_failed() throws Exception {
    // when
    JobExecution execution = jobLauncherTestUtils.launchJob();

    // then
    assertThat(execution.getExitStatus().getExitCode()).isEqualTo("FAILED");
    assertThat(execution.getExitStatus().getExitDescription())
        .contains("Skip limit")
        .contains("강제적으로 Reader에서 장애를 일으킴");
  }

}
