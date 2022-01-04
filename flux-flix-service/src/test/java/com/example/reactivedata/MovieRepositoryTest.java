package com.example.reactivedata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@DataMongoTest
class MovieRepositoryTest {

	@Autowired
	private MovieRepository repository;

	@Test
	void queryTest() {
		var results = repository
				.deleteAll()
				.thenMany(Flux.just("AAA", "BBB", "CCC", "CCC")
						.map(title -> new Movie(null, title, "horror"))
						.flatMap(aMovie -> repository.save(aMovie)))
				.thenMany(repository.findByTitle("CCC"));

		StepVerifier
			.create(results)
			.expectNextCount(2)
			.verifyComplete();
	}

}
