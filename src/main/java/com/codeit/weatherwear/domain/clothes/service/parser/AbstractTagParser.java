package com.codeit.weatherwear.domain.clothes.service.parser;

import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Slf4j
public abstract class AbstractTagParser implements SiteParser {

  protected ClothesDto buildFromMetaTag(Document document, String siteName) {
    String name = document.title();
    String imageUrl = extractOgImage(document);

    log.info("[Extracting Cloth Completed : {}, Name: {}", siteName, name);
    return ClothesDto.builder().name(name).imageUrl(imageUrl).build();
  }

  private String extractOgImage(Document document) {
    Element ogImageTag = document.selectFirst("meta[property=og:image]");
    return ogImageTag != null ? ogImageTag.attr("content") : null;
  }
}
