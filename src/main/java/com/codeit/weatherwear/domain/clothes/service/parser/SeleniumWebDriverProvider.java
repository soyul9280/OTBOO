package com.codeit.weatherwear.domain.clothes.service.parser;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
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
        "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36");
    options.setPageLoadStrategy(PageLoadStrategy.NONE);
    options.setPageLoadTimeout(Duration.ofSeconds(15));

  }

  public WebDriver getWebDriver() {
    return new ChromeDriver(options);
  }

}
