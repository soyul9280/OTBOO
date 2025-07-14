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
public class TwentyNineCmParser implements SiteParser {

  @Override
  public boolean supports(String url) {
    return url != null && url.matches("https?://(www\\.)?29cm\\.co\\.kr/.*");
  }

  @Override
  public void waitUntilReady(WebDriver driver) {
    log.info("[Start Extracting 29cm Cloth]");
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    wait.until(ExpectedConditions.presenceOfElementLocated(
        By.id("pdp_product_name")
    ));
    wait.until(ExpectedConditions.presenceOfElementLocated(
        By.cssSelector("img[src*='img.29cm.co.kr/item']")
    ));
  }

  @Override
  public ClothesDto extract(WebDriver driver) {
    //상품명, 대표이미지 추출 ( 없으면 빈값 )
    String productName = extractText(driver, By.id("pdp_product_name"), "productName");
    String imageUrl = extractAttribute(driver, By.cssSelector("img[src*='img.29cm.co.kr/item']"),
        "src", "imageUrl");
    log.info("[Extracting Cloth Completed : {}, Name: {}", "29cm", productName);
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
