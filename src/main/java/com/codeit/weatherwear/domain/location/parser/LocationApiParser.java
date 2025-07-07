package com.codeit.weatherwear.domain.location.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LocationApiParser {

  private static final List<String> ADDRESS_DEPTH_NAME_LIST = List.of(
      "region_1depth_name", "region_2depth_name", "region_3depth_name"
  );

  public List<String> parse(String responseBody) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(responseBody);
      JsonNode address = root.path("documents").get(0).path("address");

      if (address.isMissingNode()) {
        // todo: exception
        throw new RuntimeException("주소 정보가 없습니다");
      }

      return ADDRESS_DEPTH_NAME_LIST.stream()
          .map(key -> address.path(key).asText(""))
          .collect(Collectors.toList());
    } catch (JsonProcessingException jpe) {
      // todo: 예외 처리
      log.error("JSON 파싱 실패: {}", jpe.getMessage());
      throw new RuntimeException();
    }
  }


}
