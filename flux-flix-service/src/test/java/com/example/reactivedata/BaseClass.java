package com.example.reactivedata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.restassured.RestAssured;
import reactor.core.publisher.Flux;

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
