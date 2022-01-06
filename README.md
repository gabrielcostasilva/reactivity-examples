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

As the `Movie` entity does not have any reactive implementation, its test does not require any special implementation other than traditional _assertions_. The code below presents the full entity test class.

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

For the first test, a `Movie` instance is created with valid values (2). Next, assertions check whether the instance `id` is not null (3) and the `genre` corresponds to the one informed when the instance was created (4). 

Notice that the first assertion uses Junit `Assertions.assertNull` whereas the second assertion uses AssertJ `Assertions.assertThat`. AssertJ provides a more fluent API, in my opinion.

The second test uses AssertJ `Assertions.assertThatExceptionOfType` to check whether an `IllegalArgumentException` is thrown (6) and a specific message is delivered (8) when the instance is provided with a `null` title (7).

## Repository
As Josh Long presented, there are two strategies for testing MongoDB repositories:  `ReactiveMongoTemplate` or the traditional repository-based testing. 

Although I could not figure out the advantage of using `ReactiveMongoTemplate`, [the official documentation](https://spring.getdocs.org/en-US/spring-data-docs/spring-data-mongodb/reference/mongo.reactive/mongo.reactive.template.html) states that "A major difference between the two APIs is that `ReactiveMongoOperations` can be passed domain objects instead of `Document`, and there are fluent APIs for `Query`, `Criteria`, and `Update` operations instead of populating a `Document` to specify the parameters for those operations."

The snippet of code below shows the `ReactiveMongoTemplate` strategy for testing whether the `save()` method works as expected. Note that this **is not** something one should be concerned with. However, this is just an exercise.

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

The main difference between this second and the first test strategy is that this second strategy uses the `MovieRepository` instead of the `ReactiveMongoTemplate`. The code snippet below highlights main differences.

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

1. Deletes existent data.

2. Persists four `Movie` instances based on given titles.

3. Uses the `findByTitle(String): Flux<Movie>` method to retrieve `Movie`s that match the given title.

4. Checks whether two `Movie` records were found.

## Service
Josh's presentation uses a different example that does not depend on a service layer. However, I noticed that `FluxFlixService` service class does nothing but wrapping _repository_ operations and creating a `Flux<MovieEvent>` based on simple calculations. 

I have not noticed anything that is fundamentally different from the tests already done in here. **If you would like to try, go ahead and create a pull request with the test.**

## Controller
The code below presents the full `MovieRestController` test. 

```java
package com.example.reactivedata;

 import org.junit.jupiter.api.Test;
 import org.junit.jupiter.api.extension.ExtendWith;
 import org.mockito.Mockito;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
 import org.springframework.boot.test.mock.mockito.MockBean;
 import org.springframework.http.MediaType;
 import org.springframework.test.context.junit.jupiter.SpringExtension;
 import org.springframework.test.web.reactive.server.WebTestClient;

 import reactor.core.publisher.Flux;

 @ExtendWith(SpringExtension.class)
 @WebFluxTest // (1)
 class MovieRestControllerTest {

 	@MockBean // (2)
 	private FluxFlixService service; // (2)

 	@Autowired
 	private WebTestClient client; // (3)

 	@Test
 	public void test() {
 		Mockito // (2)
 			.when(this.service.findAll())
 			.thenReturn(
 				Flux.just(
 						new Movie(null, "Hello World", "Comedy"), 
 						new Movie(null, "Look at me", "Drama")));

 		this.client
 				.get()
 				.uri("/movies") // (4)
 				.exchange()
 				.expectStatus().isOk() // (5)
 				.expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON) // (6)
 				.expectBody() // (7)
 					.jsonPath("@.[0].title").isEqualTo("Hello World")
 					.jsonPath("@.[1].title").isEqualTo("Look at me");
 	}

 }
```

1. As in the `repository` test, we load only what is necessary for this test rather than loading the entire application context. In this case, we do not load anything related to the database. Only configuration [relevant to WebFlux test is loaded](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/test/autoconfigure/web/reactive/WebFluxTest.html).

2. `MovieRestController` uses `FluxFlixService` as an API to access services. Therefore, we _mock_ this service. Notice that the first line in the `test()` method sets the expected return for the method that the controller is going to use.

3. Injects a `WebTestClient`. The annotation uses the `WebClient` [to access the API](https://howtodoinjava.com/spring-webflux/webfluxtest-with-webtestclient/). The `WebClient` is a non blocking [alternative to RestTemplate](https://www.baeldung.com/spring-webclient-resttemplate). Notice that the `WebTestClient` is auto-configured [thanks to `@WebFluxText`](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/test/autoconfigure/web/reactive/WebFluxTest.html).

4. Sets the URL that will be called.

5. Sets the expected response status.

6. Sets the expected response content type.

7. Sets the expected body content. Notice that it uses JSON path to query the JSON response searching for expected movie titles.


## Client
To test the client, first we had to fix the lack of MongoDB dependency by adding `spring-boot-starter-data-mongodb` to the `pom.xml`. Without this dependency, the `Movie` entity annotation `@Document` could not resolve.

Next, the `spring-cloud-contract-wiremock` dependency to enable mocking a REST API. Please checkout the [commit](https://github.com/gabrielcostasilva/reactivity-examples/commit/fe5bf6d5c0f32cb67ed9246bffb9e7d8a53bab5a) for details.

Finally, we had to [move the client code to its own class](https://github.com/gabrielcostasilva/reactivity-examples/commit/f1722e09cb3070fd788e450b327ac1d214eb3112) instead of keeping the code in the `main` method. 

The code below shows the full client test.

```java
package com.example.reactivedata;

 import java.util.List;
 import java.util.function.Predicate;

 import org.junit.jupiter.api.Test;
 import org.junit.jupiter.api.extension.ExtendWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
 import org.springframework.boot.test.context.SpringBootTest;
 import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.MediaType;
 import org.springframework.test.context.junit.jupiter.SpringExtension;

 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.github.tomakehurst.wiremock.client.WireMock;

 import reactor.test.StepVerifier;

 @ExtendWith(SpringExtension.class)
 @AutoConfigureWireMock(port = 8080) // (1)
 @AutoConfigureJson // (3)
 @SpringBootTest
 class MovieWebClientTest {

 	@Autowired
 	private MovieWebClient client;

 	@Autowired
 	private ObjectMapper objectMapper; // (4)

 	@Test
 	public void test() throws Exception {

 		var movies = List.of(
 				new Movie(null, "Hello World", "Horror"), 
 				new Movie(null, "Here I am", "Comedy"));

 		var json = objectMapper.writeValueAsString(movies); // (5)

 		WireMock.stubFor( /// (2)
 				WireMock.get(WireMock.urlEqualTo("/movies"))
 			.willReturn(
 					WireMock.aResponse()
 						.withBody(json) // (6)
 						.withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
 						.withStatus(HttpStatus.OK.value())));

 		StepVerifier // (8)
 			.create(this.client.getAllMovies())
 			.expectNextMatches(predicate(null, "Hello World", "Horror"))
 			.expectNextMatches(predicate(null, "Here I am", "Comedy"))
 			.verifyComplete();
 	}

 	Predicate<Movie> predicate(String id, String title, String genre) { // (7)
 		return movie -> movie.title().equalsIgnoreCase(title);
 	}

 }
```
1. Auto-configures [WireMock](https://github.com/wiremock/wiremock) to mocking REST calls. WireMock is part of [Spring Cloud Contract](https://cloud.spring.io/spring-cloud-contract/2.0.x/multi/multi__spring_cloud_contract_wiremock.html), which is necessary in the [dependencies](https://github.com/gabrielcostasilva/reactivity-examples/commit/fe5bf6d5c0f32cb67ed9246bffb9e7d8a53bab5a).

2. Uses WireMock for creating a mock URL that will return a expected body, header and status.

3. Auto-configures the imports for JSON tests. Interestingly, the [official documentation](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/test/autoconfigure/json/AutoConfigureJson.html) urges to use `@JsonTest` instead. As the `AutoConfigureWireMock` annotation, this annotation is part of the [slice-based test](https://stackoverflow.com/questions/66437493/spring-boot-how-auto-configure-works-and-jsontest) that Spring enables.

4. Injects and [ObjectMapper to enable](https://www.baeldung.com/jackson-object-mapper-tutorial) serializing and deserializing JSON objects.

5. Creates a JSON object based on a list of `Movie`s.

6. Sets the JSON object as a body for the mock REST API response.

7. Creates a convenience method to evaluate expected values.

8. Checks for expected values.