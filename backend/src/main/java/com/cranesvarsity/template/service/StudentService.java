package com.cranesvarsity.template.service;

import com.cranesvarsity.template.model.Student;
import com.cranesvarsity.template.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Business logic lives here — keep controllers thin.
 * Layering: Controller -> Service -> Repository -> Database
 */
@Service
public class StudentService {

    private final StudentRepository repository;

    // Constructor injection (preferred over @Autowired on fields)
    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    public List<Student> findAll() {
        return repository.findAll();
    }

    public Optional<Student> findById(Long id) {
        return repository.findById(id);
    }

    public Student create(Student student) {
        return repository.save(student);
    }

    public Optional<Student> update(Long id, Student incoming) {
        return repository.findById(id).map(existing -> {
            existing.setName(incoming.getName());
            existing.setEmail(incoming.getEmail());
            existing.setCourse(incoming.getCourse());
            existing.setProgress(incoming.getProgress());
            existing.setStatus(incoming.getStatus());
            return repository.save(existing);
        });
    }


    public boolean delete(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }
}
