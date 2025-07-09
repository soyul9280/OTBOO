package com.codeit.weatherwear.domain.directmessage.controller;

import com.codeit.weatherwear.domain.directmessage.DirectMessageService;
import com.codeit.weatherwear.domain.directmessage.dto.DirectMessageDto;
import com.codeit.weatherwear.domain.directmessage.dto.request.DirectMessageCreateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

  private final DirectMessageService directMessageService;

  @MessageMapping("direct-messages_send")
  public DirectMessageDto sendDirectMessage(
      @Payload DirectMessageCreateRequest directMessageCreateRequest
  ) {
    return directMessageService.create(directMessageCreateRequest);
  }
}
