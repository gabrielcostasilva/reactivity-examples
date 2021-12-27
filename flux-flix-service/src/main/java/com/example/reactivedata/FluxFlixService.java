package com.example.reactivedata;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.Date;
import java.util.Random;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class FluxFlixService {

    private final MovieRepository movieRepository;

    public Flux<MovieEvent> streamStreams(Movie movie) {
        var interval = Flux.interval(Duration.ofSeconds(1));
        var events = Flux.fromStream(Stream.generate(() -> new MovieEvent(movie, new Date(), randomUser())));
        return Flux.zip(interval, events).map(Tuple2::getT2);
    }

    public Flux<Movie> findAll() {
        return movieRepository.findAll();
    }

    public Mono<Movie> findById(String id) {
        return movieRepository.findById(id);
    }

    public String randomUser() {
        String users[] = "John, Phillip, Maycon, Wilson, Maria, Johnson".split(",");
        return users[new Random().nextInt(users.length)];
    }
}

