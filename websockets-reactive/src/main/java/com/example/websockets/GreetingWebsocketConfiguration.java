package com.example.websockets;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

@Configuration
public class GreetingWebsocketConfiguration {

	@Autowired
	private WebSocketHandler greetingsHandler;

	@Bean
	public HandlerMapping handlerMapping() {
		var map = new HashMap<String, WebSocketHandler>();
		map.put("/ws/greetings", greetingsHandler);
		int order = -1;

		return new SimpleUrlHandlerMapping(map, order);
	}

	@Bean
	public WebSocketHandlerAdapter handlerAdapter() {
		return new WebSocketHandlerAdapter();
	}
}
