package com.devops.app;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskRepository repository;

    public TaskController(TaskRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Task> list() {
        return repository.findAll();
    }

    @PostMapping
    public ResponseEntity<Task> create(@RequestBody Task task) {
        if (task.getTitle() == null || task.getTitle().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        task.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(task));
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<Task> toggle(@PathVariable Long id) {
        return repository.findById(id).map(t -> {
            t.setDone(!t.isDone());
            return ResponseEntity.ok(repository.save(t));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
