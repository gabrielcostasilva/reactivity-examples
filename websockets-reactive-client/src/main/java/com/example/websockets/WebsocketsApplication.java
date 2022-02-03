package com.example.websockets;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;

import reactor.core.publisher.Mono;

@SpringBootApplication
public class WebsocketsApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebsocketsApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void init() {
		var client = new ReactorNettyWebSocketClient();

		client.execute(
			URI.create("ws://localhost:8080/ws/greetings"), 
			session -> session.send(
				Mono.just(session.textMessage("A made up name")))
				.thenMany(session.receive()
					.map(WebSocketMessage::getPayloadAsText)
					.log())
				.then())
			.block(Duration.ofSeconds(10L));
	}

}
