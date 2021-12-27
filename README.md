# Reactive Programming Examples
Projects in this repo show examples of how to use the reactive stack in the Spring ecosystem to develop reactive Web applications.

## Overview of Projects
Each folder groups a single example as follows:

- [_very-first-reactivity-example_](./very-first-reactivity-example/) uses WebFlux to developing a useless reactive Web app. The project consists of a publisher (`reactor.core.publisher.Flux`) that publishes even numbers to a single subscriber (`System.out.println`) responsible for printing out the numbers.

- [_reactive-data_](./reactive-data/) accesses a MongoDB database, delete all content and create new documents. This project uses the reactive MongoDB repository (`org.springframework.data.mongodb.repository.ReactiveMongoRepository`) to persist a Java `record` `Movie` class, representing persistent movie data. `ReactiveMongoRepository.delete()` and `ReactiveMongoRepository.save()` subscribe to one single action.

- [flux-flix-service](./flux-flix-service/) introduces a service and a controller on top of the repository. Although controller and service mainly act as a facade, enabling access to repository data, they also feature a streaming service that demonstrates the production of infinite data streams.

- [functional-reactive-endpoints](./functional-reactive-endpoints/) replaces the traditional controller annotations int the FluxFlix project with functional reactive endpoints.

