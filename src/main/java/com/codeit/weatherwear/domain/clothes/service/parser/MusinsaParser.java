package com.codeit.weatherwear.domain.clothes.service.parser;

import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
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
    String productName = "";
    String thumbnailImageUrl = "";
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

          productName = jsonNode.get("goodsNm").asText();
          thumbnailImageUrl =
              mssCdnImageUrlPrefix + jsonNode.get("thumbnailImageUrl").asText();
          log.info("[Extracting Cloth Completed : {}, Name: {}", "무신사", productName);
          return ClothesDto.builder()
              .name(productName)
              .imageUrl(thumbnailImageUrl)
              .build();
        }
        throw new ExtractionNotFoundException();
      }
    } catch (IOException e) {
      //clothService에서 커스텀 처리 진행
      throw new RuntimeException(e);
    }
    return ClothesDto.builder()
        .name(productName)
        .imageUrl(thumbnailImageUrl)
        .build();
  }
}
