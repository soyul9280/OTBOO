package com.codeit.weatherwear.domain.directmessage;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.weatherwear.domain.directmessage.dto.DirectMessageDto;
import com.codeit.weatherwear.domain.directmessage.dto.request.DirectMessageCreateRequest;
import com.codeit.weatherwear.domain.directmessage.repository.DirectMessageRepository;
import com.codeit.weatherwear.domain.user.entity.User;
import com.codeit.weatherwear.domain.user.repository.UserRepository;
import com.codeit.weatherwear.global.config.TestContainerConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestContainerConfig.class)
public class DirectMessageIntegrationTest {

  @LocalServerPort
  int port;

  @Autowired
  UserRepository userRepository;

  @Autowired
  DirectMessageRepository directMessageRepository;

  @Autowired
  ObjectMapper objectMapper;

  BlockingQueue<DirectMessageDto> queue;

  UUID receiverId;
  UUID senderId;

  @BeforeEach
  void setUp() {
    queue = new LinkedBlockingQueue<>(1);
    User alice = User.builder()
        .email("alice@alice.com")
        .name("alice")
        .password("alice1234")
        .build();
    User bob = User.builder()
        .email("bob@bob.com")
        .name("bob")
        .password("bob1234")
        .build();

    userRepository.save(alice);
    userRepository.save(bob);

    senderId = alice.getId();
    receiverId = bob.getId();
  }

  @AfterEach
  void tearDown() {
    directMessageRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void sendDirectMessage() throws ExecutionException, InterruptedException, TimeoutException {
    StompSession session = connect();

    String destination;
    if (receiverId.toString().compareTo(senderId.toString()) < 0) {
      destination = String.format("/sub/direct-messages_%s_%s", receiverId, senderId);
    } else {
      destination = String.format("/sub/direct-messages_%s_%s", senderId, receiverId);
    }

    session.subscribe(destination, new StompFrameHandler() {
      @Override
      public Type getPayloadType(StompHeaders headers) {
        return DirectMessageDto.class;
      }
      @Override
      public void handleFrame(StompHeaders headers, Object payload) {
        queue.add((DirectMessageDto) payload);
      }
    });

    DirectMessageCreateRequest request = new DirectMessageCreateRequest(
        receiverId, senderId, "test");
    session.send("/pub/direct-messages_send", request);

    DirectMessageDto message = queue.poll(5, TimeUnit.SECONDS);

    assertThat(message).isNotNull();
    assertThat(message.content()).isEqualTo(request.content());
    assertThat(message.receiver().userId()).isEqualTo(receiverId);
    assertThat(message.sender().userId()).isEqualTo(senderId);
  }

  private StompSession connect() throws InterruptedException, ExecutionException, TimeoutException {
    WebSocketStompClient webSocketStompClient = webSocketStompClient();
    String url = String.format("ws://localhost:%d/ws", port);

    WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
    headers.setBasicAuth("user", "password");

    StompHeaders connectHeaders = new StompHeaders();
    connectHeaders.setLogin("user");
    connectHeaders.setPasscode("password");

    return webSocketStompClient
        .connectAsync(url, headers, connectHeaders, new StompSessionHandlerAdapter() {})
        .get(5, TimeUnit.SECONDS);
  }

  private WebSocketStompClient webSocketStompClient() {
    StandardWebSocketClient standardWebSocketClient = new StandardWebSocketClient();
    WebSocketTransport webSocketTransport = new WebSocketTransport(standardWebSocketClient);
    List<Transport> transports = Collections.singletonList(webSocketTransport);
    SockJsClient sockJsClient = new SockJsClient(transports);

    WebSocketStompClient webSocketStompClient = new WebSocketStompClient(sockJsClient);
    webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter(objectMapper));

    return webSocketStompClient;
  }
}
