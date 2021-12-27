package com.example.reactivedata;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

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

    @Bean
    RouterFunction<ServerResponse> routes(FluxFlixService service) {
        return route(GET("/movies"),
                request -> ok().body(service.findAll(), Movie.class))

                .andRoute(GET("/movies/{id}"),
                        request -> ok().body(service.findById(request.pathVariable("id")), Movie.class))

                .andRoute(GET("/movies/{id}/events"),
                        request -> ok().contentType(MediaType.TEXT_EVENT_STREAM)
                                .body(service.findById(request.pathVariable("id")).flatMapMany(service::streamStreams),
                                        MovieEvent.class));
    }
}
