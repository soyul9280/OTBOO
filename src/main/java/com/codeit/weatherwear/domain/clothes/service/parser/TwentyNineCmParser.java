package com.codeit.weatherwear.domain.clothes.service.parser;

import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TwentyNineCmParser extends AbstractTagParser {

  @Override
  public boolean supports(String url) {
    return url != null && url.matches("https?://(www\\.)?29cm\\.co\\.kr/.*");
  }

  @Override
  public ClothesDto extract(Document document) {
    log.info("[Start Extracting 29cm Cloth]");
    return buildFromMetaTag(document, "29CM");
  }

}
