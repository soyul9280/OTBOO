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
public class TwentyNineCmParser implements SiteParser{

  @Override
  public boolean supports(String url) {
    return url.contains("29cm");
  }

  @Override
  public void waitUntilReady(WebDriver driver) {
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    wait.until(ExpectedConditions.presenceOfElementLocated(
        By.id("pdp_product_name")
    ));
  }

  @Override
  public ClothesDto extract(WebDriver driver) {
    //상품명, 대표이미지 추출 ( 없으면 빈값 )
    String productName = "";
    String imageUrl = "";
    try {
      WebElement nameElement = driver.findElement(By.id("pdp_product_name"));
      productName = nameElement.getText();
    } catch (NoSuchElementException ignored) {}

    try {
      WebElement imageElement = driver.findElement(By.cssSelector("img[src*='img.29cm.co.kr/item']"));
      imageUrl = imageElement.getAttribute("src");
    } catch (NoSuchElementException ignored) {}

    return ClothesDto.builder()
        .name(productName)
        .imageUrl(imageUrl)
        .build();
  }

}
