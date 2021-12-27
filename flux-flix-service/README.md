# Reactive FluxFlix Example
In this example, Josh Long extends the reactive Spring Data/MongoDB with a [service and controller](https://www.youtube.com/watch?v=zVNIZXf4BG8).

## Project Overview
This project introduces the `FluxFlixService` service on top of the `MovieRepository` repository, and the `MovieRestController` controller on top of the the `FluxFlixService` service. Both controller and service act as a _facade_, enabling access to repository data.  

For example, the snippet code below shows the `FluxFlixService.findAll()` method (1) accessing the repository. Similarly, the controller has also a method that accesses all movies in the service (2).

```
@RequiredArgsConstructor
@Service
public class FluxFlixService {

    private final MovieRepository movieRepository;

    (...)
    
    public Flux<Movie> findAll() { // (1)
        return movieRepository.findAll();
    }
    
    (...)
} 

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieRestController {

    private final FluxFlixService fluxFlixService;
    
    (...)

    @GetMapping
    public Flux<Movie> findAll() { // (2)
        return fluxFlixService.findAll();
    }
    
    (...)
}
```

However, this project also features a streaming service, implemented in the `FluxFlixService`, as follows:

```
public Flux<MovieEvent> streamStreams(Movie movie) { // (1)
    var interval = Flux.interval(Duration.ofSeconds(1));  // (2)
    var events = Flux.fromStream(Stream.generate(() -> new MovieEvent(movie, new Date(), randomUser()))); // (3)
    return Flux.zip(interval, events).map(Tuple2::getT2); // (4)
}
```

1. The `streamStreams(Movie)` method returns a `Flux` of `MovieEvent`. A `MovieEvent` is a Java `record` carrying the movie itself, a `java.util.Date` representing when the event occurred, and a random user.
2. The event is published in 1 sec interval.
3. A `java.util.stream.Stream` infinitely generates `MovieEvent`. A `Flux` wraps these events.
4. `inverval` and `events` publishers are zipped together, and returned as the method result.

As the service publishes the results, all we need is a controller to stream the results to an endpoint, as the `MovieRestController.events(String)` does:

```
@GetMapping(value = "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE) (1)
public Flux<MovieEvent> events(@PathVariable String id) {
    return fluxFlixService.findById(id).flatMapMany(fluxFlixService::streamStreams); (2)
}
```

1. Produces a continuous datatype.
2. Calls the stream service method. 