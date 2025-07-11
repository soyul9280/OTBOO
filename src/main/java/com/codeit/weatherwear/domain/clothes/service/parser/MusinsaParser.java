package com.codeit.weatherwear.domain.clothes.service.parser;

import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import java.time.Duration;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MusinsaParser implements SiteParser {

  @Override
  public boolean supports(String url) {
    return url.contains("musinsa");
  }

  @Override
  public void waitUntilReady(WebDriver driver) {
    log.info("[Start Extracting Musinsa Cloth]");
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    wait.until(ExpectedConditions.presenceOfElementLocated(
        By.cssSelector("div[data-content-name='대표이미지'] img[alt][src]")
    ));
  }

  @Override
  public ClothesDto extract(WebDriver driver) {
    //상품명, 대표이미지 추출 ( 없으면 빈값 )
    String productName = "";
    String imageUrl = "";

    try {
      WebElement mainImageContainer = driver.findElement(
          By.cssSelector("div[data-content-name='대표이미지']"));
      WebElement imageElement = mainImageContainer.findElement(By.tagName("img"));
      imageUrl = imageElement.getAttribute("src");
      productName = imageElement.getAttribute("alt");
    } catch (NoSuchElementException ignored) {
    }

    log.info("[Extracting Cloth Completed : {}, Name: {}", "무신사", productName);

    return ClothesDto.builder()
        .name(productName)
        .imageUrl(imageUrl)
        .build();
  }
}
