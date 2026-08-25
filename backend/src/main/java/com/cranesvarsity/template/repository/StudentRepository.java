package com.cranesvarsity.template.repository;

import com.cranesvarsity.template.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA gives you findAll / findById / save / deleteById for free —
 * you do not write any SQL for basic CRUD.
 *
 * You can add "derived queries" just by naming the method, e.g.:
 *   List<Student> findByStatus(String status);
 *   List<Student> findByCourseAndStatus(String course, String status);
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findByStatus(String status);
}
