# Reactive Programming Examples - Contract Testing Branch
Projects in this branch extends the [testing branch](https://github.com/gabrielcostasilva/reactivity-examples/tree/testing) to showing consumer-driven contract testing. As for the [testing branch](https://github.com/gabrielcostasilva/reactivity-examples/tree/testing), [Josh Long's presentation](https://www.youtube.com/watch?v=N24JZi-xFx0) is the main reference for this branch.


## Issues When Testing Distributed Systems
One issue when testing distributed systems is that consumer and provider interfaces must agree for a smooth integration. The test strategy used in the testing branch does not contribute for a smooth integration because we used mocks to integrating the [flux-flix-client](https://github.com/gabrielcostasilva/reactivity-examples/tree/testing/flux-flix-client) into [flux-flix-service](https://github.com/gabrielcostasilva/reactivity-examples/tree/testing/flux-flix-service) project.

Mocks do a good job by enabling testing whether the consumer can consume data from the provider, and the provider provides data for consumption. However, mocks do not ensure that consumer and provider adopts the same communication interface.

In his presentation, Josh Long shows a good example of communication interface mismatch, in which the provider uses a `name` attribute whereas the consumer uses a `reservationName` attribute. Both sides pass their individual tests, but they fail when the communication must be established.

## Consumer-Driven Contract Testing
Consumer-driven contract testing uses a contract to assert that consumer and provider agrees with the same interface. 

As usual, Spring has the [Spring Cloud Contract](https://spring.io/projects/spring-cloud-contract#overview) project that makes consumer-driven contract easy to implement. In addition to dependencies, the project also provides a plugin that breaks the build if the API does not comply with the established contract.


## Implementing the Contract
First, we add dependencies to the `pom.xml` of the provider project (`flux-flix-service`), as follows:

```xml
(...)

<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-contract-verifier</artifactId>
	<scope>test</scope>
</dependency>

(...)

<dependencyManagement>
	<dependencies>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-dependencies</artifactId>
			<version>2021.0.0</version>
			<type>pom</type>
			<scope>import</scope>
		</dependency>
	</dependencies>
</dependencyManagement>

(...)
```

[Second](https://github.com/gabrielcostasilva/reactivity-examples/commit/49253e966084dd123e6adc9442f10da5f901bf47), we create the contract in the `flux-flix-service/src/test/resources/contracts/shouldReturnAllMovies.groovy` file. In the example below, we used Groovy as Josh did in his presentation. However, I found it difficult to work with Groovy as I do not understand the language. One option is using [YAML instead](https://spring.io/blog/2018/02/13/spring-cloud-contract-in-a-polyglot-world). 

```groovy
package contracts

 import org.springframework.cloud.contract.spec.Contract
 import org.springframework.http.HttpStatus
 import org.springframework.http.MediaType

 
  Contract.make { // (1)
     description("should return all Movies")
     request { // (2)
         url ("/movies") // (3)
         method GET() // (4)
     }
     response { // (5)
         body( //(6)
             [
                 [id: 1, title: "Hello World", genre: "Horror"],
                 [id: 2, title: "Look at me", genre: "Drama"]
             ]
         )
         status(HttpStatus.OK.value()) // (7)
         headers{ // (8)
             contentType(MediaType.APPLICATION_JSON_VALUE)
         }
     }
  } 
```
1. Defines the contract
2. Sets the expected request
3. Sets the request URL that will be called
4. Sets the request method that will be used
5. Sets the expected response
6. Sets the response body content
7. Sets the response status
8. Sets the response headers

Notice that this code represents one single contract. One can create as many contracts as necessary.

[Next](https://github.com/gabrielcostasilva/reactivity-examples/commit/f548d6a3624d33be9e9d8193b88a27dde6079176), we need to create a test class that will load what is necessary for testing the provider API. 

For instance, the `MovieRestController` uses the `FluxFlixService`. In the `MovieRestControllerTest`, we used a mock for injecting the `FluxFlixService`. Therefore, we need to create a class that sets the mocks and everything else that is necessary. In this project, the class `BaseClass` is responsible for setting what we need for the contract test, as one can see below.

```java
(...)

@SpringBootTest(
    properties = "server.port=0",
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = ReactiveDataApplication.class
)
@ExtendWith(SpringExtension.class)
public class BaseClass {

    @InjectMocks
    private FluxFlixService service;

    @MockBean
    private MovieRepository repository;

    @LocalServerPort
    private int port;

    @BeforeEach
    public void before() {        
        Mockito
        .when(this.repository.findAll())
        .thenReturn(
            Flux.just(
                    new Movie(null, "Hello World", "Horror"), 
                    new Movie(null, "Look at me", "Drama")));
                                
        RestAssured.baseURI = "http://localhost:" + port;
    }
    
}
```

[Then](https://github.com/gabrielcostasilva/reactivity-examples/commit/e2e22b93058ce87f4fac1c7568aa2fe92b26de36), we need to add the cloud contract plugin and identify the base class in the configuration, as follows:

```xml
<plugin>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-contract-maven-plugin</artifactId>
	<version>3.1.0</version>
	<extensions>true</extensions>
	<configuration>
		<baseClassForTests>com.example.reactivedata.BaseClass</baseClassForTests>
	</configuration>
</plugin>
```

Run `mvn install` to make the contract available locally. The contract will be transpilled into a new test class, as the code below shows. Notice that the `ContractVerifierTest` class extends the `BaseClass` we created previously. You can check the generated test class in your terminal with `find . -iname ContractVerifierTest.java`.


```java
package com.example.reactivedata;

import com.example.reactivedata.BaseClass;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import io.restassured.response.ResponseOptions;

import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat;
import static org.springframework.cloud.contract.verifier.util.ContractVerifierUtil.*;
import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.*;

@SuppressWarnings("rawtypes")
public class ContractVerifierTest extends BaseClass {

	@Test
	public void validate_shouldReturnAllMovies() throws Exception {
		// given:
			MockMvcRequestSpecification request = given();


		// when:
			ResponseOptions response = given().spec(request)
					.get("/movie");

		// then:
			assertThat(response.statusCode()).isEqualTo(200);
			assertThat(response.header("Content-Type")).matches("application/json.*");

		// and:
			DocumentContext parsedJson = JsonPath.parse(response.getBody().asString());
			assertThatJson(parsedJson).array().contains("['id']").isEqualTo(1);
			assertThatJson(parsedJson).array().contains("['title']").isEqualTo("Helo World");
			assertThatJson(parsedJson).array().contains("['genre']").isEqualTo("Horror");
			assertThatJson(parsedJson).array().contains("['id']").isEqualTo(2);
			assertThatJson(parsedJson).array().contains("['title']").isEqualTo("Look at me");
			assertThatJson(parsedJson).array().contains("['genre']").isEqualTo("Comedy");
	}

}
```
If the build process is successfully concluded, it means the provider complies with the contract and that the contract is now available for testing the consumer.

As before, to test the consumer/client we need the `spring-cloud-contract-stub-runner` dependency. Notice that we no longer need the `spring-cloud-contract-wiremock` dependency as the wiremock test was removed in this version.

For testing the client, we removed all references from the wiremock and added: 

```java
(...)

@AutoConfigureStubRunner(
	stubsMode = StubRunnerProperties.StubsMode.LOCAL,
	ids = "com.example:flux-flix-service:+:8080"
)

(...)
```

The annotation instructs to use the local Maven repository for finding the `groupId` and `artifactId` set as `ids`. If the test pass, it means the client complies with the API.