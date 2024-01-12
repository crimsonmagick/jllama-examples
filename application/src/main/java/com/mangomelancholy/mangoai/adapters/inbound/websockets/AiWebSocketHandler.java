package com.mangomelancholy.mangoai.adapters.inbound.websockets;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class AiWebSocketHandler implements WebSocketHandler {

  @Override
  public Mono<Void> handle(final WebSocketSession webSocketSession) {
    return webSocketSession.send(Flux.interval(Duration.of(10, ChronoUnit.SECONDS))
            .map(s -> webSocketSession.textMessage(s.toString())))
        .and(webSocketSession.receive()
            .map(WebSocketMessage::getPayloadAsText)
            .log());
  }
}