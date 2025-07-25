package com.codeit.weatherwear.global.config;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;

public class ContainerInitializer implements
    ApplicationContextInitializer<ConfigurableApplicationContext> {

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("weatherwear")
            .withUsername("user")
            .withPassword("password")
            .withReuse(false);
    KafkaContainer kafka = new KafkaContainer("apache/kafka:4.0.0")
        .withReuse(false);
    postgres.start();
    kafka.start();

    // 애플리케이션 컨텍스트가 닫힐 때 컨테이너 정리
    applicationContext.registerShutdownHook();
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      postgres.stop();
      kafka.stop();
    }));

    TestPropertyValues.of(
        "spring.datasource.url=" + postgres.getJdbcUrl(),
        "spring.datasource.username=" + postgres.getUsername(),
        "spring.datasource.password=" + postgres.getPassword(),
        "spring.kafka.bootstrap-servers=" + kafka.getBootstrapServers()
    ).applyTo(applicationContext.getEnvironment());
  }
}