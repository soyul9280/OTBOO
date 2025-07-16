package com.codeit.weatherwear.domain.clothes.service.parser;

import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import java.time.Duration;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ZigZagParser implements SiteParser {

  @Override
  public boolean supports(String url) {
    return url.toLowerCase().contains("zigzag.kr") && (url.contains("/catalog/") || url.contains(
        "/shop/"));
  }

  @Override
  public void waitUntilReady(WebDriver driver) {
    log.info("[Start Extracting ZigZag Cloth]");
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    wait.until(ExpectedConditions.presenceOfElementLocated(
        By.cssSelector("h1[class*='BODY_15']")
    ));
    wait.until(ExpectedConditions.presenceOfElementLocated(
        By.cssSelector("img[src*='cf.product-image.s.zigzag.kr']")
    ));
  }

  @Override
  public ClothesDto extract(WebDriver driver) {
    //상품명, 대표이미지 추출 ( 없으면 빈값 )
    String productName = extractText(driver, By.cssSelector("h1[class*='BODY_15']"), "productName");
    String imageUrl = extractAttribute(driver,
        By.cssSelector("img[src*='cf.product-image.s.zigzag.kr']"), "src", "imageUrl");
    log.info("[Extracting Cloth Completed : {}, Name: {}", "지그재그", productName);
    return ClothesDto.builder()
        .name(productName)
        .imageUrl(imageUrl)
        .build();
  }

  private String extractText(WebDriver driver, By selector, String label) {
    try {
      return driver.findElement(selector).getText();
    } catch (NoSuchElementException e) {
      log.warn("[Fail Extracting Cloth] Not Found {}: {}", label, e.getMessage());
      throw e;
    }
  }

  private String extractAttribute(WebDriver driver, By selector, String attr, String label) {
    try {
      return driver.findElement(selector).getAttribute(attr);
    } catch (NoSuchElementException e) {
      log.warn("[Fail Extracting Cloth] Not Found {}: {}", label, e.getMessage());
      throw e;
    }
  }
}
