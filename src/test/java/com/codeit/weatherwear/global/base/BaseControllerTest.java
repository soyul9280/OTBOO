package com.codeit.weatherwear.global.base;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.codeit.weatherwear.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest
@Import({GlobalExceptionHandler.class})
@ActiveProfiles("test")
@WithMockUser
public abstract class BaseControllerTest {

  @Autowired
  protected MockMvc mockMvc;
  @Autowired
  private WebApplicationContext webApplicationContext;


  @BeforeEach
  void setUp() {
    this.mockMvc = MockMvcBuilders
        .webAppContextSetup(webApplicationContext)
        .apply(springSecurity())
        // 요청에 유효한 CSRF 토큰 포함
        .defaultRequest(post("/**").with(csrf()))
        .defaultRequest(put("/**").with(csrf()))
        .defaultRequest(patch("/**").with(csrf()))
        .defaultRequest(delete("/**").with(csrf()))
        .build();
  }
}
