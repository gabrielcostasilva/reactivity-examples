# Reactive Programming Examples
Projects in this repo show examples of how to use the reactive stack in the Spring ecosystem to develop reactive Web applications.

## Overview of Projects
Each folder groups a single example as follows:

- _very-first-reactivity-example_ uses WebFlux to developing a useless reactive Web app. The project consists of a publisher (`reactor.core.publisher.Flux`) that publishes even numbers to a single subscriber (`System.out.println`) responsible for printing out the numbers.

- _reactive-data_ accesses a MongoDB database, delete all content and create new documents. This project uses the reactive MongoDB repository (`org.springframework.data.mongodb.repository.ReactiveMongoRepository`) to persist a Java `record` `Movie` class, representing persistent movie data. `ReactiveMongoRepository.delete()` and `ReactiveMongoRepository.save()` subscribe to one single action.
