package com.codeit.weatherwear.domain.clothes.service.parser;

import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import com.codeit.weatherwear.domain.clothes.exception.cloth.ExtractionException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ZigZagParser implements SiteParser {

  private final SeleniumWebDriverProvider webDriverProvider;

  @Override
  public boolean supports(String url) {
    return url != null && url.matches("https?://(www\\.)?zigzag\\.kr/.*");
  }

  @Override
  public ClothesDto extract(String url, Document document) {
    log.info("[Start Extracting ZigZag Cloth]");
    WebDriver driver = webDriverProvider.getWebDriver();
    try {
      driver.get(url);
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
      wait.until(
          ExpectedConditions.presenceOfElementLocated(By.cssSelector("meta[property='og:image']")));

      String name = driver.getTitle();
      String imageUrl = driver.findElement(By.cssSelector("meta[property='og:image']"))
          .getAttribute("content");

      log.info("[Extracting Cloth Completed] : 지그재그, Name: {}", name);
      return ClothesDto.builder().name(name).imageUrl(imageUrl).build();
    } catch (NoSuchElementException e) {
      log.warn("[Fail Extracting Cloth] Not Found: {}", e.getMessage());
      throw e;
    } finally {
      driver.quit();
    }
  }
}
