package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.model.Student;
import com.cranesvarsity.template.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Full CRUD example. Because application.properties sets
 * server.servlet.context-path=/api, the real URLs are:
 *
 *   GET    http://localhost:8080/api/students
 *   GET    http://localhost:8080/api/students/1
 *   POST   http://localhost:8080/api/students
 *   PUT    http://localhost:8080/api/students/1
 *   DELETE http://localhost:8080/api/students/1
 *
 * Copy this file to build your own endpoints.
 */
@RestController
@RequestMapping("/students")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @GetMapping
    public List<Student> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getOne(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Student create(@RequestBody Student student) {
        return service.create(student);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Student> update(@PathVariable Long id, @RequestBody Student student) {
        return service.update(id, student)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
