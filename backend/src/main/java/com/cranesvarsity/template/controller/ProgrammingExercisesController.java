package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.ProgrammingExerciseItem;
import com.cranesvarsity.template.service.ProgrammingExercisesService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/student/practice/programming-exercises")
public class ProgrammingExercisesController {

    private final ProgrammingExercisesService service;

    public ProgrammingExercisesController(ProgrammingExercisesService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProgrammingExerciseItem> list() {
        return service.list();
    }
}
