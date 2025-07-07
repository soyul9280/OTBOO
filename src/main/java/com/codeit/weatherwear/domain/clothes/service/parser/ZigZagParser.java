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
public class ZigZagParser implements SiteParser{

  @Override
  public boolean supports(String url) {
    return url.contains("zigzag");
  }

  @Override
  public void waitUntilReady(WebDriver driver) {
    log.info("[지그재그 옷 정보 추출 시작]");
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
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
    String productName = "";
    String imageUrl = "";
    try {
      WebElement nameElement = driver.findElement(By.cssSelector("h1[class*='BODY_15']"));
      productName = nameElement.getText();
    } catch (NoSuchElementException ignored) {}

    try {
      WebElement imageElement = driver.findElement(By.cssSelector("img[src*='cf.product-image.s.zigzag.kr']"));
      imageUrl = imageElement.getAttribute("src");
    } catch (NoSuchElementException ignored) {}

    log.info("[옷 정보 추출 완료 : {}, 상품명: {}","지그재그", productName);
    return ClothesDto.builder()
        .name(productName)
        .imageUrl(imageUrl)
        .build();
  }
}
