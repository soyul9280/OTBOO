package com.codeit.weatherwear.domain.clothes.service.parser;

import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ZigZagParser implements SiteParser {

  @Override
  public boolean supports(String url) {
    return url.toLowerCase().contains("zigzag.kr");
  }

  @Override
  public ClothesDto extract(Document document) {
    log.info("[Start Extracting ZigZag Cloth]");
    String productName;
    String thumbnailImageUrl;

    productName = document.title();
    // 썸네일 (og:image)
    Element ogImageTag = document.selectFirst("meta[property=og:image]");
    thumbnailImageUrl = ogImageTag != null ? ogImageTag.attr("content") : null;
    log.info("[Extracting Cloth Completed : {}, Name: {}", "지그재그", productName);
    return ClothesDto.builder()
        .name(productName)
        .imageUrl(thumbnailImageUrl)
        .build();
  }
}
