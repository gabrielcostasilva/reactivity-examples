package com.example.reactivedata;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

@SpringBootApplication
@RequiredArgsConstructor
public class ReactiveDataApplication {

	private final MovieRepository movieRepository;

	public static void main(String[] args) {
		SpringApplication.run(ReactiveDataApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void init() {
		Runnable create = () -> Stream.of("Aeon Flux",
				"Enter the Mono<Void>",
				"The Fluxinator",
				"The Silence of the Lambdas",
				"Reactive Mongos on Plane")
				.map(name -> new Movie(UUID.randomUUID().toString(), name, randomGenre()))
				.forEach(m -> movieRepository.save(m).subscribe(System.out::println));

		movieRepository.deleteAll().subscribe(null, null, create);
	}

	private String randomGenre() {
		String genres[] = "horror,romcom,drama,action,documentary".split(",");
		return genres[new Random().nextInt(genres.length)];
	}
}
