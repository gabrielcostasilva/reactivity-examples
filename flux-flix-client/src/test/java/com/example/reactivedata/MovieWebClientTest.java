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
@AutoConfigureWireMock(port = 8080)
@AutoConfigureJson
@SpringBootTest
class MovieWebClientTest {
	
	@Autowired
	private MovieWebClient client;
	
	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void test() throws Exception {
		
		var movies = List.of(
				new Movie(null, "Hello World", "Horror"), 
				new Movie(null, "Here I am", "Comedy"));
		
		var json = objectMapper.writeValueAsString(movies);
		
		WireMock.stubFor(
				WireMock.get(WireMock.urlEqualTo("/movies"))
			.willReturn(
					WireMock.aResponse()
						.withBody(json)
						.withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withStatus(HttpStatus.OK.value())));
		
		StepVerifier
			.create(this.client.getAllMovies())
			.expectNextMatches(predicate(null, "Hello World", "Horror"))
			.expectNextMatches(predicate(null, "Here I am", "Comedy"))
			.verifyComplete();
	}
	
	Predicate<Movie> predicate(String id, String title, String genre) {
		return movie -> movie.title().equalsIgnoreCase(title);
	}

}
