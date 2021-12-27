# Reactive FluxFlix Example With Functional Reactive Endpoints
In this example, Josh Long replaces the traditional controller in the [FluxFlix Example](https://www.youtube.com/watch?v=zVNIZXf4BG8) with _functional reactive endpoints_.

## Project Overview
This project builds on top of the FluxFlix project by replacing the traditional controller annotations with functional reactive endpoints.

For comparison's sake, we mapped before and after code. This is the **previous controller**:

```
@RestController
@RequestMapping("/movies")
(...)
public class MovieRestController {

    private final FluxFlixService fluxFlixService;

    @GetMapping(value = "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE) // (1)
    public Flux<MovieEvent> events(@PathVariable String id) {
        return fluxFlixService.findById(id).flatMapMany(fluxFlixService::streamStreams);
    }

    @GetMapping // (2)
    public Flux<Movie> findAll() {
        return fluxFlixService.findAll();
    }

    @GetMapping("/{id}") //(3)
    public Mono<Movie> findById(@PathVariable String id) {
        return fluxFlixService.findById(id);

    }
}
```

This is the **functional reactive endpoint**:

```
(...)
@SpringBootApplication
public class ReactiveDataApplication {

    (...)

    @Bean
    RouterFunction<ServerResponse> routes(FluxFlixService service) {
        return route(GET("/movies"),
                request -> ok().body(service.findAll(), Movie.class)) //(1)

                .andRoute(GET("/movies/{id}"),
                        request -> ok().body(service.findById(request.pathVariable("id")), Movie.class)) //(2)

                .andRoute(GET("/movies/{id}/events"),
                        request -> ok().contentType(MediaType.TEXT_EVENT_STREAM)
                                .body(service.findById(request.pathVariable("id")).flatMapMany(service::streamStreams),
                                        MovieEvent.class)); // (3)
    }
}
```

However, the benefit of using functional reactive endpoints is not clear to me at this point. 
