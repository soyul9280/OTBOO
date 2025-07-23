package com.codeit.weatherwear.domain.clothes.service.parser;

import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TwentyNineCmParser implements SiteParser {

  @Override
  public boolean supports(String url) {
    return url != null && url.matches("https?://(www\\.)?29cm\\.co\\.kr/.*");
  }

  @Override
  public ClothesDto extract(Document document) {
    log.info("[Start Extracting 29cm Cloth]");
    String productName = "";
    String thumbnailImageUrl = "";

    productName = document.title();
    // 썸네일 (og:image)
    Element ogImageTag = document.selectFirst("meta[property=og:image]");
    thumbnailImageUrl = ogImageTag != null ? ogImageTag.attr("content") : null;
    log.info("[Extracting Cloth Completed : {}, Name: {}", "29cm", productName);
    return ClothesDto.builder()
        .name(productName)
        .imageUrl(thumbnailImageUrl)
        .build();
  }

}
