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
  public ClothesDto extract(String url, Document document) {
    log.info("[Start Extracting 29cm Cloth]");
    String name = document.title();
    String imageUrl = extractOgImage(document);

    log.info("[Extracting Cloth Completed] : {}, Name: {}", "29CM", name);
    return ClothesDto.builder().name(name).imageUrl(imageUrl).build();
  }

  private String extractOgImage(Document document) {
    Element ogImageTag = document.selectFirst("meta[property=og:image]");
    return ogImageTag != null ? ogImageTag.attr("content") : null;
  }
}
