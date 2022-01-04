package com.example.reactivedata;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;

@Repository
public interface MovieRepository extends ReactiveMongoRepository<Movie, String> {
	Flux<Movie> findByTitle(String title);
}
