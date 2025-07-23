package com.codeit.weatherwear.domain.clothes.service.parser;

import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import org.jsoup.nodes.Document;

public interface SiteParser {

  boolean supports(String url);

  ClothesDto extract(Document document);
}
