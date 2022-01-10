package com.example.reactivedata;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@AutoConfigureStubRunner(
	stubsMode = StubRunnerProperties.StubsMode.LOCAL,
	ids = "com.example:flux-flix-service:+:8080"
)
@SpringBootTest
class MovieWebClientTest {
	
	@Autowired
	private MovieWebClient client;

	@Test
	public void test() throws Exception {
		
		StepVerifier
			.create(this.client.getAllMovies())
			.expectNextMatches(predicate(null, "Hello World", "Horror"))
			.expectNextMatches(predicate(null, "Look at me", "Drama"))
			.verifyComplete();
	}
	
	Predicate<Movie> predicate(String id, String title, String genre) {
		return movie -> movie.title().equalsIgnoreCase(title);
	}

}
