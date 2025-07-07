package com.codeit.weatherwear.domain.clothes.service.parser;

import com.codeit.weatherwear.domain.clothes.dto.response.ClothesDto;
import java.util.Optional;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class TwentyNineCmParser implements SiteParser{

  @Override
  public boolean supports(String url) {
    return url.contains("29cm.com");
  }

  @Override
  public ClothesDto extract(Document document) {
    //상품명, 대표이미지 추출 ( 없으면 빈값 )
    String productName = Optional.ofNullable(
        document.getElementById("pdp_product_name")
    ).map(Element::text).orElse("");

    String imageUrl = Optional.ofNullable(
        document.selectFirst("img[src*='img.29cm.co.kr/item']")
    ).map(img -> img.attr("src")).orElse("");



    return ClothesDto.builder()
        .name(productName)
        .imageUrl(imageUrl)
        .build();
  }

}
