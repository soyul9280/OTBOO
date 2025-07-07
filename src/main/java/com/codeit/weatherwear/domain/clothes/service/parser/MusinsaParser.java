package com.codeit.weatherwear.domain.clothes.service.parser;

import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import java.util.Optional;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class MusinsaParser implements SiteParser {

  @Override
  public boolean supports(String url) {
    return url.contains("musinsa.com");
  }

  @Override
  public ClothesDto extract(Document document) {
    //상품명, 대표이미지 추출 ( 없으면 빈값 )
    String productName = Optional.ofNullable(
        document.selectFirst("span[data-mds='Typography'].text-title_18px_med")
    ).map(Element::text).orElse("");

    String imageUrl = Optional.ofNullable(
        document.selectFirst("img[src*='image.msscdn.net']")
    ).map(el -> el.attr("src")).orElse("");

    return ClothesDto.builder()
        .name(productName)
        .imageUrl(imageUrl)
        .build();
  }
}
