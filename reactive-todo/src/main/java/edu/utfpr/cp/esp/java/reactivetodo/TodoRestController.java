package edu.utfpr.cp.esp.java.reactivetodo;

import java.time.Duration;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class TodoRestController {

    private final TodoRepository repository;

    public TodoRestController (TodoRepository repository) {
        this.repository = repository;
    }
    
    @GetMapping("/todos")
    public ResponseEntity<Flux<Todo>> lerTodos() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/todos/{feito}")
    public ResponseEntity<Flux<Todo>> lerByFeito(@PathVariable boolean feito)  {
        return ResponseEntity.ok(repository.findByFeito(feito));
    }

    @PostMapping("/todo")
    public ResponseEntity<Mono<Todo>> criar(@RequestBody Todo todo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(todo));
    }

    @DeleteMapping("/todo/{id}")
    public ResponseEntity deletar(@PathVariable String id) {
        
        repository.deleteById(id).onTerminateDetach();
        return ResponseEntity.ok().build();
    }

    @PutMapping("/todo/{id}")
    public ResponseEntity<Mono<Todo>> atualizar(@PathVariable String id) {

        return ResponseEntity
                .ok(repository
                    .findById(id)
                    .map(todoAtual ->  new Todo(id, 
                                                todoAtual.titulo(), 
                                                todoAtual.descricao(), 
                                                !todoAtual.feito()))
                    .flatMap(repository::save)
                    .onTerminateDetach());
    }

    
}
