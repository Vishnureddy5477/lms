package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.McqPracticeTestItem;
import com.cranesvarsity.template.service.McqPracticeTestsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/student/practice/mcq-tests")
public class McqPracticeTestsController {

    private final McqPracticeTestsService service;

    public McqPracticeTestsController(McqPracticeTestsService service) {
        this.service = service;
    }

    @GetMapping
    public List<McqPracticeTestItem> list() {
        return service.list();
    }
}
