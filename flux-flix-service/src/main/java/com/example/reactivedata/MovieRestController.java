package com.example.reactivedata;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.awt.*;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieRestController {

    private final FluxFlixService fluxFlixService;

    @GetMapping(value = "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<MovieEvent> events(@PathVariable String id) {
        return fluxFlixService.findById(id).flatMapMany(fluxFlixService::streamStreams);
    }

    @GetMapping
    public Flux<Movie> findAll() {
        return fluxFlixService.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Movie> findById(@PathVariable String id) {
        return fluxFlixService.findById(id);

    }
}
