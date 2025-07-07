package com.codeit.weatherwear.domain.clothes.service.parser;

import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import java.time.Duration;
import java.util.NoSuchElementException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

@Component
public class MusinsaParser implements SiteParser {

  @Override
  public boolean supports(String url) {
    return url.contains("musinsa");
  }

  @Override
  public void waitUntilReady(WebDriver driver) {
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    wait.until(ExpectedConditions.presenceOfElementLocated(
        By.cssSelector("span.text-title_18px_med")
    ));
    wait.until(ExpectedConditions.presenceOfElementLocated(
        By.cssSelector("div[data-content-name='대표이미지']")
    ));
  }

  @Override
  public ClothesDto extract(WebDriver driver) {
    //상품명, 대표이미지 추출 ( 없으면 빈값 )
    String productName = "";
    String imageUrl = "";

    try {
      WebElement nameElement = driver.findElement(
          By.cssSelector("span.text-title_18px_med")
      );
      productName = nameElement.getText();
    } catch (NoSuchElementException ignored) {}

    try {
      WebElement mainImageContainer = driver.findElement(By.cssSelector("div[data-content-name='대표이미지']"));
      WebElement imageElement = mainImageContainer.findElement(By.tagName("img"));
      imageUrl = imageElement.getAttribute("src");
    } catch (NoSuchElementException ignored) {}

    return ClothesDto.builder()
        .name(productName)
        .imageUrl(imageUrl)
        .build();
  }
}
