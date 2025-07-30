package com.codeit.weatherwear.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        // 기본 정보
        .info(new Info()
            .title("옷장을 부탁해 API 문서")
            .description("옷장을 부탁해 프로젝트의 Swagger API 문서입니다.")
            .contact(new Contact()
                .name("sb01-team10")
                .url("https://github.com/sb01-team10/sb01-otboo-team10"))
            .version("1.0.0")
        )
        // 인증 방식 설정
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
        .components(new Components().addSecuritySchemes("bearerAuth",
            new SecurityScheme()
                .name("Authorization")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
        ))
        .servers(List.of(
            new Server().url("http://localhost:8080").description("로컬 서버")
        ));
  }
}
