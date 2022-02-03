# WebSocket Example
This project is based on [Josh Long's excellent intro to WebSocket](https://www.youtube.com/watch?v=m0K3ElazGE0).

WebSocket is an [HTTP-compatible protocol](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#websocket) using default 80 and 443 ports. Therefore, leveraging firewall rules. 

It features two-way message communication between client and server. Thus, WebSocket is intended for real-time applications.

In contrast with REST that has several endpoints, a WebSocket usually has only a single endpoint for establishing the connection, which is kept open until the client or the server decides to close it. Another difference is that WebSocket requires another protocol (on top of itself) to set content, format and type of exchanged messages. In this project, [STOMP](https://stomp.github.io/stomp-specification-1.2.html#Abstract) is used.

## Project Overview
The `WebSocketController` class sets the endpoint to connect client and server. The controller receives a `GreetingRequest` and returns a `GreetingResponse` through the `greet()` method. If something goes wrong, the `handleException()` method is invoked.

Both `GreetingRequest` and `GreetingResponse` are simple Java POJOS, implemented as `record`, as the code below exemplifies.

```java
public record GreetingsRequest(String name) { }
```

The `greet()` method is the heart of the controller. The method body checks whether the first letter of the input name is capitalised and then returns a greeting, as the snippet below shows.

```java
Assert.isTrue(Character.isUpperCase(request.name().charAt(0)), () -> "The name must start with a capital letter"); // Just to show off the handleException() method

Thread.sleep(1_000); // Just to give the feeling of an asynchronous call
		
return new GreetingsResponse("Hello, " + request.name() + " !");	
```

More important than the body are the annotations on top of the method. `@org.springframework.messaging.handler.annotation.MessageMapping` sets the server endpoint, whereas `org.springframework.messaging.handler.annotation.SendTo` sets the topic where messages are sent to. 

Spring uses [STOMP](https://stomp.github.io/stomp-specification-1.2.html#Abstract) as [a sub-protocol on top of WebSocket](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#websocket-stomp) for defining the content, type and format of exchanged messages. Therefore, acting as a message broker. This requires configuration that is set by `GreetingWebsocketConfiguration` class, as the code below shows.

```java
@Configuration // (1)
@EnableWebSocketMessageBroker // (2)
public class GreetingWebsocketConfiguration implements WebSocketMessageBrokerConfigurer { // (3)

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) { 
		registry.enableSimpleBroker("/topic"); // (4)
		registry.setApplicationDestinationPrefixes("/app"); //(5)
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/chat").withSockJS(); // (6) 
	}
}
```
1. Identifies this as a configuration class.
2. Identifies the class as a message broker.
3. Inherits common methods for message broker classes.
4. Sets an in-memmory subscription topic. Messages with `/topic` headers as routed to the broker.
5. Sets the destination header (`/app`) to
`@MessageMapping` methods in the controller.
6. Sets the HTTP server endpoint. It uses SockJS, which enables WebSock emulation when there are impediments for WebSocket interations.

To try out, the [`/resources/static/index.html`](./src/main/resources/static/index.html) file uses JavaScript with SockJS and STOMP libraries to connect to the server and exchange messages & simulate exception.

Finally, `spring-boot-starter-websocket` is required to enable Spring Boot WebSocket features.

## Further Reference

- [Spring guide](https://spring.io/guides/gs/messaging-stomp-websocket/) - Simple get started.
- [Intro to WebSock with Spring](https://www.baeldung.com/websockets-spring) - More JS examples.
- [Using Spring Boot for WebSocket Implementation with STOMP](https://www.toptal.com/java/stomp-spring-boot-websocket) - More complete.