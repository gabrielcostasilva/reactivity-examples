# Reactive Programming Examples - Testing Branch
Projects in this branch show how to test the entire stack of a reactive Web application with Spring Boot. The main reference for this branch is [Josh Long's presentation](https://www.youtube.com/watch?v=N24JZi-xFx0).

## Entity
As implemented in this project, the `Movie` entity is just a a Java `record`. Actually, there is nothing to test in a simple `record`. Therefore, I added a validation for the movie title, as the code below shows.

```java
import org.springframework.data.mongodb.core.mapping.Document;

 @Document
 public record Movie(String id, String title, String genre){ 

 	public Movie {
 		if ((title == null) || (title.length() <= 2)) {
 			throw new IllegalArgumentException("Invalid title. Checkout whether the title is informed and longer than 2 characters.");
 		}
 	}
 }
```

The validation verifies whether the title is informed and longer than 2 characters. The validation throws an `IllegalArgumentException` if constraints are not satisfied.

As the `Movie` entity does not have any reactive implementation, its test does not require any special implementation other than simple _assertions_. The code below presents the full entity test class.

```java
package com.example.reactivedata;

 import static org.assertj.core.api.Assertions.assertThat;
 import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
 import static org.junit.jupiter.api.Assertions.assertNull;

 import org.junit.jupiter.api.Test;

 class MovieTest {

 	@Test
 	public void successfullyCreateNewInstance() { // (1)
 		var instance = new Movie(null, "The return of the dead", "Horror"); // (2)
 		assertNull(instance.id()); // (3)

 		assertThat(instance.genre()).contains("Horror"); //(4)
 	}

 	@Test
 	public void invalidTitleCreateNewInstance() { // (5)
 		assertThatExceptionOfType(IllegalArgumentException.class) // (6)
 			.isThrownBy(() -> new Movie(null, null, "horror")) // (7)
 			.withMessage("Invalid title. Checkout whether the title is informed and longer than 2 characters."); // (8)
 	}
 }
```
`MovieTest` class has two methods, representing two tests. Whereas the first test checks whether valid values create a valid `Movie` instance (1), the second test checks whether the title validation works for a `null` title (5).

For the first test, a `Movie` instance is created with valid values (2). Next, assertions check whether the instance id is not null (3) and the genre corresponds to the one informed when the instance was created (4). Notice that the first assertion uses Junit `Assertions.assertNull` whereas the second assertion uses AssertJ `Assertions.assertThat`. AssertJ provides a more fluent API in my opinion.

The second test uses AssertJ `Assertions.assertThatExceptionOfType` to check whether an `IllegalArgumentException` is thrown (6) and a specific message is delivered (8) when the instance is provided with a `null` title (7).

## Repository
As Josh Long presented, there are two strategies for testing MongoDB repositories:  `ReactiveMongoTemplate` or the traditional repository-based testing. 

Although I could not figure out the advantage of using `ReactiveMongoTemplate`, [the official documentation](https://spring.getdocs.org/en-US/spring-data-docs/spring-data-mongodb/reference/mongo.reactive/mongo.reactive.template.html) states that "A major difference between the two APIs is that `ReactiveMongoOperations` can be passed domain objects instead of `Document`, and there are fluent APIs for `Query`, `Criteria`, and `Update` operations instead of populating a `Document` to specify the parameters for those operations."

The snippet of code below shows the `ReactiveMongoTemplate` strategy for testing whether the `save()` method works as expected. Note that this is not something one should be concerned with. However, this is just an exercise.

```java
(...)
 import org.junit.jupiter.api.extension.ExtendWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
 import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
 import org.springframework.test.context.junit.jupiter.SpringExtension;

 import reactor.test.StepVerifier;

 @ExtendWith(SpringExtension.class) // (1)
 @DataMongoTest // (2)
 class MovieRepositoryTemplateTest {

 	@Autowired
 	private ReactiveMongoTemplate template; // (3)

 	@Test
 	public void testSave() {
 		var movie = template.save(new Movie(null, "Back to school", "Horror")); // (4)

 		StepVerifier
 			.create(movie) // (5)
 			.expectNextMatches(aMovie -> aMovie.title().startsWith("Back") && aMovie.id() != null) // (6)
 			.verifyComplete(); //(7)
 	}

 }
```

1. [Extends](https://junit.org/junit5/docs/current/user-guide/#extensions) JUnit to seamlessly integrate with Spring TestContext framework. Note that this annotation [may not be necessary here](https://rieckpil.de/what-the-heck-is-the-springextension-used-for/).

2. Auto-configures MongoDB and loads repository-related features for the [Spring Test Context](https://rieckpil.de/spring-boot-test-slices-overview-and-usage/).

3. Injects an instance of `ReactiveMongoTemplate` that is used for testing. It enables core [MongoDB operations](https://docs.spring.io/spring-data/mongodb/docs/current/api/org/springframework/data/mongodb/core/ReactiveMongoTemplate.html).

4. Uses the `ReactiveMongoTemplate` instance to save a given `Movie`. 

5. [Creates a declarative way](https://projectreactor.io/docs/test/release/api/reactor/test/StepVerifier.html) to verify events in a `Publisher`. In this case, a `Mono<Movie>` resulting from the `ReactiveMongoTemplate.save()` method in line (4). 

6. Specifies an expectation for the movie title (_assertion_).

7. Triggers the verification.

The `MovieRepositoryTest` class implements the second test strategy. For the second test, I added a `findByTitle(String): Flux<Movie>` method into `MovieRepository`.

The main difference between this second and the first test strategy is that this one uses the `MovieRepository` instead of the `ReactiveMongoTemplate`. The code snippet below shows main differences.

```java
(...)

 import reactor.core.publisher.Flux;

(...) 

 class MovieRepositoryTest {

 	@Autowired
 	private MovieRepository repository;

 	@Test
 	void queryTest() {
 		var results = repository
 				.deleteAll() // (1)
 				.thenMany(Flux.just("AAA", "BBB", "CCC", "CCC")
 						.map(title -> new Movie(null, title, "horror"))
 						.flatMap(aMovie -> repository.save(aMovie))) // (2)
 				.thenMany(repository.findByTitle("CCC")); // (3)

 		StepVerifier
 			.create(results)
 			.expectNextCount(2) // (4)
 			.verifyComplete();
 	}

 }
```

1. Deletes all existent data.

2. Persists four `Movie` instances based on given titles.

3. Uses the `findByTitle(String): Flux<Movie>` method to retrieve `Movie`s that match the given title.

4. Checks whether two `Movie` records were found.