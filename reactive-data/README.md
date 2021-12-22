# Reactive Data With MongoDB Example
This example is based on the [Josh Long](https://www.youtube.com/watch?v=zVNIZXf4BG8) second explanation on reactive Spring with Spring Data/MongoDB.

## Project Overview
This project access a MongoDB database, delete all content and create new _documents_. The `Movie` class represents persistent movie data. It is implemented with Java `record`, as follows:

```
@Document // (1)
public record Movie(String id, String title, String genre){ } // (2)
```

1. Sets this as a MongoDB persistent entity;
2. Sets the entity name and attributes.

As usual, we need a repository for data persistence. The difference from traditional MVC/SQL Web apps is that our repository extends `org.springframework.data.mongodb.repository.ReactiveMongoRepository`. 

Finally, we need to use the repository to delete all data and then persist new ones. We start by creating a `Runnable` that creates movies from a `java.util.stream.Stream`, like so:

```
Runnable create = () -> Stream.of("Aeon Flux",
        "Enter the Mono<Void>",
        "The Fluxinator",
        "The Silence of the Lambdas",
        "Reactive Mongos on Plane")
        .map(name -> new Movie(UUID.randomUUID().toString(), name, randomGenre())) // (1)
        .forEach(m -> movieRepository.save(m).subscribe(System.out::println)); // (2)
```

1. A new `Movie` entity is created with mock data for each entry in the `java.util.stream.Stream`;
2. The `MovieRepository` persists each entity **and** subscribes to `System.out::println`, which prints out each movie data.

To deleting all existing data, we have to:

```
                    // (1)      (2)                 (3)
movieRepository.deleteAll().subscribe(null, null, create);
```

1. Call the `deleteAll` method, responsible for deleting all existing data;
2. _Subscribe_ to the `create` action. This ensures that once all data is deleted, the `create` action will be executed;
3. Specify the action that ought to be run when the delete is done.