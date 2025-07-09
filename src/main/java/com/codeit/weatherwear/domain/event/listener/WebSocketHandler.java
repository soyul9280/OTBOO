package com.codeit.weatherwear.domain.event.listener;

import com.codeit.weatherwear.domain.directmessage.dto.DirectMessageDto;
import com.codeit.weatherwear.domain.event.dto.DirectMessageReceivedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler {

  private final SimpMessagingTemplate messagingTemplate;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(DirectMessageReceivedEvent event) {
    DirectMessageDto dto = event.directMessageDto();
    String receiverId = dto.receiver().userId().toString();
    String senderId = dto.sender().userId().toString();

    String destination;

    if (receiverId.compareTo(senderId) < 0) {
      destination = String.format("/sub/direct-message_%s_%s", receiverId, senderId);
    } else {
      destination = String.format("/sub/direct-message_%s_%s", senderId, receiverId);
    }
    messagingTemplate.convertAndSend(destination, dto);
  }
}
