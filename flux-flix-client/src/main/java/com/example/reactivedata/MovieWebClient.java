package com.example.reactivedata;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;

@Component
public class MovieWebClient {
	
	public Flux<Movie> getAllMovies() {
		var client = WebClient.create();

        return client.get()
                .uri("http://localhost:8080/movies")
                .retrieve()
                .bodyToFlux(Movie.class);
	}
}
