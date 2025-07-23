package com.codeit.weatherwear.domain.clothes.service.parser;

import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
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
  public ClothesDto extract(Document document) {
    log.info("[Start Extracting ZigZag Cloth]");
    String productName;
    String thumbnailImageUrl;

    productName = Objects.requireNonNull(document.selectFirst("title")).text();
    // 썸네일 (og:image)
    thumbnailImageUrl = Objects.requireNonNull(document.selectFirst("meta[property=og:image]"))
        .attr("content");
    log.info("[Extracting Cloth Completed : {}, Name: {}", "지그재그", productName);
    return ClothesDto.builder()
        .name(productName)
        .imageUrl(thumbnailImageUrl)
        .build();
  }
}
