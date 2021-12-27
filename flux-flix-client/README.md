# Reactive FluxFlix Client
This example creates a WebClient for the [reactive FluxFlix project](https://www.youtube.com/watch?v=zVNIZXf4BG8).

## Project Overview
Usually, Spring MVC, a servlet-based Web stack, would use Spring RestTemplate to access REST APIs. For reactive servers, we should use WebClient instead. The code below shows the code used in this project. 

Notice that two calls are made, (2) and (3). However, as a reactive client, the second call (3) is only executed when call (2) returns successfully.

```
var client = WebClient.create(); (1)

client.get() // (2)
        .uri("http://localhost:8080/movies")
        .exchangeToFlux(clientResponse -> clientResponse.bodyToFlux(Movie.class))
        .filter(movie -> movie.title().toLowerCase().contains("flux".toLowerCase()))
        .subscribe(movie ->
                client.get() // (3)
                        .uri("http://localhost:8080/movies/{id}/events", movie.id())
                        .exchangeToFlux(clientResponse -> clientResponse.bodyToFlux(MovieEvent.class))
                        .subscribe(System.out::println));
```

1. Creates a WebClient client;
2. Starts the first call;
3. Starts the second call.

