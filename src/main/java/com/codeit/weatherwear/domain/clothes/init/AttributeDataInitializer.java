package com.codeit.weatherwear.domain.clothes.init;

import com.codeit.weatherwear.domain.clothes.entity.Attribute;
import com.codeit.weatherwear.domain.clothes.repository.AttributeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class AttributeDataInitializer implements ApplicationRunner {

  private final AttributeRepository attributeRepository;

  @Override
  public void run(ApplicationArguments args) {
    if (!attributeRepository.existsByName("계절")) {
      attributeRepository.save(Attribute.builder()
          .name("계절")
          .selectableValues(List.of("봄", "여름", "가을", "겨울"))
          .build());
    }

    if (!attributeRepository.existsByName("두께")) {
      attributeRepository.save(Attribute.builder()
          .name("두께")
          .selectableValues(List.of("매우 얇음", "얇음", "두꺼움", "매우 두꺼움"))
          .build());
    }
  }
}
