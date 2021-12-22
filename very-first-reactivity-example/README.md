# Very First Reactivity Example
This example is based on the [Josh Long](https://www.youtube.com/watch?v=zVNIZXf4BG8) introductory example on reactive Spring. 

## Project Overview
This very first example uses the WebFlux project to developing a useless reactive Web app. The project consists of a _publisher_ (`reactor.core.publisher.Flux`) that publishes even numbers to a single _subscriber_ (`System.out.println`) responsible for printing out the numbers.

```
Flux<String> myFluxStream = Flux.fromArray("1,2,3,4".split(",")); //(1)

myFluxStream
	.map(Integer::parseInt) // (2)
	.filter(i -> i % 2 == 0) // (2)
	.subscribe(System.out::println); //(4)
```
1. A `reactor.core.publisher.Flux` has factory methods that enable creating a set of objects from different sources, such as array or streams;
2. The `reactor.core.publisher.Flux` object provides convenience methods for mapping and filtering;
3. A _publisher_ publishes to _subscribers_. In this case, the `System.out.println` is a `Consumer` that acts as a subscriber that processes the published content.  