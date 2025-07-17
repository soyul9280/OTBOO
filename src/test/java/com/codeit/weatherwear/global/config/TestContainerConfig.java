package com.codeit.weatherwear.global.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestContainerConfig {

  private static final PostgreSQLContainer<?> POSTGRES;

  static {
    POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
        .withDatabaseName("test")
        .withUsername("test_user")
        .withPassword("test_password")
        .withEnv("POSTGRES_SHUTDOWN_TIMEOUT", "5");
  }

  @Bean(destroyMethod = "stop")
  @ServiceConnection
  public PostgreSQLContainer<?> postgreSQLContainer() {
    return POSTGRES;
  }
}