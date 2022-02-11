# WebSocket Reactive Example
This project is based on [Josh Long's excellent intro to Reactive WebSocket](https://www.youtube.com/watch?v=Z5q-CXbvM1E) (actually, the intro is on Reactive Spring, but he shows reactive WebSocket somewhere by the end of the presentation).

WebSocket is an [HTTP-compatible protocol](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#websocket) using default 80 and 443 ports. Therefore, leveraging firewall rules. It features two-way message communication between client and server. Thus, WebSocket is intended for real-time applications. In contrast with REST that has several endpoints, a WebSocket usually has only a single endpoint for establishing the connection, which is kept open until the client or the server decides to close it. 

The WebSocket feature of keeping the connection alive makes it a great candidate for reactive applications. With reactive WebSocket, the communication is kept active while the thread is not blocked.

> :information_source: To know more about WebSocket with Spring Boot, checkout [this example](https://github.com/gabrielcostasilva/sb-controllers/tree/main/websockets).

## Project Overview
This project has similarities and differences when compared with the [WebSocket example](https://github.com/gabrielcostasilva/sb-controllers/tree/main/websockets).

First, this is also a _greetings request-response-based_ example. However, in this example the response is continuous instead of a single response sent to a queue.

Second, this example consists of a [_WebSocket handler_](./src/main/java/com/example/websockets/GreetingsWebsocketHandler.java) instead of a controller.

Third, this example does not rely on STOMP or SockJS. This leads to a simpler [app client](./src/main/resources/static/index.html).

On the other hand, this example also has a [configuration class](./src/main/java/com/example/websockets/GreetingWebsocketConfiguration.java). 

### Handler
The [com.example.websockets.GreetingsWebsocketHandler](./src/main/java/com/example/websockets/GreetingsWebsocketHandler.java) class has two methods. The first is the `Flux<GreetingsResponse> greet(GreetingsRequest)` method, responsible for creating the a reactive response based on the request. The code snippet below shows the method content. The code creates a continuous response every second.

```java
return Flux
	.fromStream(
		Stream.generate(() -> 
			new GreetingsResponse(
				"hello" + request.name() + " @ " + Instant.now())))
	.delayElements(Duration.ofSeconds(1));
```
The second method handles the WebSocket session, creating the WebSocket server. The method is fully presented below. The method is inherited from the `org.springframework.web.reactive.socket.WebSocketHandler` interface, which the handler class implements.

```java
@Override
public Mono<Void> handle(WebSocketSession session) {
	return session.send( // (1)
		session
			.receive() // (2)
			.map(WebSocketMessage::getPayloadAsText) // (3)
			.map(GreetingsRequest::new)
			.flatMap(this::greet)
			.map(GreetingsResponse::name)
			.map(session::textMessage)
	);
}
```
1. Sends a message to the client in this WebSocket session.
2. Starts a receive message process.
3. Maps returns to send a greeting back as a continuous response.
     
### Configuration
Finally, the [`com.example.websockets.GreetingWebsocketConfiguration`](./src/main/java/com/example/websockets/GreetingWebsocketConfiguration.java) configuration class injects the WebSocket handler and sets the URL.

### Client
The application client just needs a URL to connect to. The code is simple as:

```javascript
window.addEventListener('load', function(e) {
	let ws = new WebSocket('ws://localhost:8080/ws/greetings')

	ws.addEventListener('open', function() {
		let name = window.prompt('Inform name')
		ws.send(name)
	})

	ws.addEventListener('message', function(msg) {
		console.log('Greeting: ' + msg.data)
	})
})
```

## Further Reference
- [Official documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-websocket)
- [Reactive WebSockets with Spring 5](https://www.baeldung.com/spring-5-reactive-websockets)