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
@WebFluxTest
class MovieRestControllerTest {

	@MockBean
	private FluxFlixService service;
	
	@Autowired
	private WebTestClient client;

	@Test
	public void test() {
		Mockito
			.when(this.service.findAll())
			.thenReturn(
				Flux.just(
						new Movie(null, "Hello World", "Comedy"), 
						new Movie(null, "Look at me", "Drama")));
		
		this.client
				.get()
				.uri("/movies")
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
				.expectBody()
					.jsonPath("@.[0].title").isEqualTo("Hello World")
					.jsonPath("@.[1].title").isEqualTo("Look at me");
	}

}
