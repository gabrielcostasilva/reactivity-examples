package com.example.websockets;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class GreetingsWebsocketHandler implements WebSocketHandler {

	@Override
	public Mono<Void> handle(WebSocketSession session) {
		return session.send(
			session
				.receive()
				.map(WebSocketMessage::getPayloadAsText)
				.map(GreetingsRequest::new)
				.flatMap(this::greet)
				.map(GreetingsResponse::name)
				.map(session::textMessage)
		);
	}

	Flux<GreetingsResponse> greet(GreetingsRequest request) {

		return Flux
			.fromStream(Stream.generate(() -> new GreetingsResponse("hello" + request.name() + " @ " + Instant.now())))
		.delayElements(Duration.ofSeconds(1));
	}

	
}
