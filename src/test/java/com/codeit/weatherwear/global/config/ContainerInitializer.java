package com.codeit.weatherwear.global.config;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;

public class ContainerInitializer implements
    ApplicationContextInitializer<ConfigurableApplicationContext> {

  private static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:17-alpine")
          .withDatabaseName("weatherwear")
          .withUsername("user")
          .withPassword("password")
          .withInitScript("schema.sql");

  private static final KafkaContainer KAFKA = new KafkaContainer("apache/kafka:4.0.0");

  static {
    POSTGRES.start();
    KAFKA.start();
  }

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    TestPropertyValues.of(
        "spring.datasource.url=" + POSTGRES.getJdbcUrl(),
        "spring.datasource.username=" + POSTGRES.getUsername(),
        "spring.datasource.password=" + POSTGRES.getPassword(),
        "spring.kafka.bootstrap-servers=" + KAFKA.getBootstrapServers()
    ).applyTo(applicationContext.getEnvironment());
  }
}