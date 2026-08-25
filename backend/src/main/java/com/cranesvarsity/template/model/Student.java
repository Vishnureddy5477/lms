package com.cranesvarsity.template.model;

import jakarta.persistence.*;

/**
 * Sample entity. Hibernate creates the `students` table from this class
 * (because spring.jpa.hibernate.ddl-auto=update).
 *
 * Copy this file to create your own entities.
 */
@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 100)
    private String course;

    private Integer progress;

    @Column(length = 30)
    private String status;

    // JPA needs a no-arg constructor
    public Student() {
    }

    public Student(String name, String email, String course, Integer progress, String status) {
        this.name = name;
        this.email = email;
        this.course = course;
        this.progress = progress;
        this.status = status;
    }

    // ── Getters / Setters ─────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
