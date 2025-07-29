package com.codeit.weatherwear.domain.clothes.service.parser;

import java.time.Duration;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

@Component
public class SeleniumWebDriverProvider {

  private final ChromeOptions options;

  public SeleniumWebDriverProvider() {
    options = new ChromeOptions();
    options.addArguments("--headless=new");
    options.addArguments("--lang=ko");
    options.addArguments("--disable-gpu");
    options.addArguments("--no-sandbox");
    options.addArguments(
        "--user-agent=Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Mobile Safari/537.36");
    options.setPageLoadStrategy(PageLoadStrategy.EAGER);
    options.setPageLoadTimeout(Duration.ofSeconds(15));
  }

  public WebDriver getWebDriver() {
    return new ChromeDriver(options);
  }

}
