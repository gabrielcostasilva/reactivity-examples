package com.example.veryfirstreactivityexample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class VeryFirstReactivityExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(VeryFirstReactivityExampleApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void init() {
		Flux<String> myFluxStream = Flux.fromArray("1,2,3,4".split(","));

		myFluxStream
				.map(Integer::parseInt)
				.filter(i -> i % 2 == 0)
				.subscribe(System.out::println);
	}
}
