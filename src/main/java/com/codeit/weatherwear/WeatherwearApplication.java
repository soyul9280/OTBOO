package com.codeit.weatherwear;

import jakarta.annotation.PreDestroy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WeatherwearApplication {

  public static void main(String[] args) {
    SpringApplication.run(WeatherwearApplication.class, args);
  }

  @PreDestroy
  public void onShutdown() {
    System.out.println(">>>>> Graceful shutdown started...");
  }

}
