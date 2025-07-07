package com.codeit.weatherwear.domain.clothes.service.parser;

import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import java.util.Optional;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class ZigZagParser implements SiteParser{

  @Override
  public boolean supports(String url) {
    return url.contains("zigzag.com");
  }

  @Override
  public ClothesDto extract(Document document) {
    //상품명, 대표이미지 추출 ( 없으면 빈값 )
    String productName = Optional.ofNullable(
        document.selectFirst("h1[class*='BODY_15']")
    ).map(Element::text).orElse("");

    String imageUrl = Optional.ofNullable(
        document.selectFirst("img[src*='cf.product-image.s.zigzag.kr']")
    ).map(el -> el.attr("src")).orElse("");


    return ClothesDto.builder()
        .name(productName)
        .imageUrl(imageUrl)
        .build();
  }
}
