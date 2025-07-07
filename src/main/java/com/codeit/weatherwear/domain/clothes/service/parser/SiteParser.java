package com.codeit.weatherwear.domain.clothes.service.parser;

import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import org.openqa.selenium.WebDriver;

public interface SiteParser {
  boolean supports(String url);
  void waitUntilReady(WebDriver driver);
  ClothesDto extract(WebDriver driver);
}
