package com.example.reactivedata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@DataMongoTest
class MovieRepositoryTemplateTest {

	@Autowired
	private ReactiveMongoTemplate template;

	@Test
	public void testSave() {
		var movie = template.save(new Movie(null, "Back to school", "Horror"));
		StepVerifier
			.create(movie)
			.expectNextMatches(aMovie -> aMovie.title().startsWith("Back") && aMovie.id() != null)
			.verifyComplete();
	}

}
