package com.codeit.weatherwear.domain.clothes.service.parser;

import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import com.codeit.weatherwear.domain.clothes.exception.cloth.ExtractionException;
import com.codeit.weatherwear.domain.clothes.exception.cloth.ExtractionNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MusinsaParser implements SiteParser {

  @Value("${clothes.extraction.musinsa.image-url-prefix}")
  private String mssCdnImageUrlPrefix;

  @Override
  public boolean supports(String url) {
    return url != null && url.matches("https?://(www\\.)?musinsa\\.com/.*");
  }

  @Override
  public ClothesDto extract(Document document) {
    log.info("[Start Extracting Musinsa Cloth]");
    try {
      Element scriptTag = document.selectFirst("script#pdp-data");
      if (scriptTag != null) {
        String scriptData = scriptTag.html();
        int stateIndex = scriptData.indexOf("window.__MSS__.product.state = ");
        if (stateIndex != -1) {
          int jsonStart = scriptData.indexOf("{", stateIndex);
          int jsonEnd = scriptData.indexOf("};", jsonStart) + 1;
          String jsonString = scriptData.substring(jsonStart, jsonEnd);
          ObjectMapper objectMapper = new ObjectMapper();
          JsonNode jsonNode = objectMapper.readTree(jsonString);

          JsonNode goodsNmNode = jsonNode.get("goodsNm");
          JsonNode thumbnailNode = jsonNode.get("thumbnailImageUrl");

          if (goodsNmNode == null || thumbnailNode == null) {
            throw new ExtractionNotFoundException();
          }

          String productName = goodsNmNode.asText();
          String thumbnailImageUrl =
              mssCdnImageUrlPrefix + thumbnailNode.asText();
          log.info("[Extracting Cloth Completed : {}, Name: {}", "무신사", productName);
          return ClothesDto.builder()
              .name(productName)
              .imageUrl(thumbnailImageUrl)
              .build();
        }
      }
      throw new ExtractionException();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
