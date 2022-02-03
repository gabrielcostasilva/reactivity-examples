# Reactive WebSocket Example - Client
The main difference between this project and the [Reactive WebSocket Example](../websockets-reactive/) is that this project shows a Java Spring client. This project is based on [this tutorial](https://www.baeldung.com/spring-5-reactive-websockets).

## Project Overview
The `init()` method below is part of `com.example.websockets.WebsocketsApplication` class. This code plays the WebSocket client role.

```java
@EventListener(ApplicationReadyEvent.class)
public void init() {
	var client = new ReactorNettyWebSocketClient(); // (1)

	client.execute( // (2)
		URI.create("ws://localhost:8080/ws/greetings"), // (3)
		session -> session.send( // (4)
			Mono.just(session.textMessage("A made up name"))) // (5)
			.thenMany(session.receive() // (6)
				.map(WebSocketMessage::getPayloadAsText) // (7)
				.log()) // (8)
			.then()) // (9)
		.block(Duration.ofSeconds(10L)); // (10)
}
```

1. Creates a `org.springframework.web.reactive.socket.client.WebSocketClient` implementation for Netty server. [Netty](https://netty.io) is the default Web container for reactive applications with WebFlux.
2. Requests a WebSocket connection to a given URI, and sets a `org.springframework.web.reactive.socket.WebSocketHandler` for handling the WebSocket session. Notice this method returns a `reactor.core.publisher.Mono<Void>`, which is used in comment (10)
3. Sets the WebSocket URI to connecting.
4. Sends a message to the server as soon as the connection is established through the `org.springframework.web.reactive.socket.WebSocketSession`.
5. Uses a `reactor.core.publisher.Mono` constructor to creating a publisher for the text message _A made up name_.
6. Uses the same session to receive the server return ...
7. ... which is mapped to text ...
8. ... and printed out as a log.
9. From the [documentation](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html): _Return a Mono<Void> which only replays complete and error signals from this Mono_
10. Forces disconnection after 10 secs timeout.


