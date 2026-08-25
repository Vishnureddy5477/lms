package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.McqPracticeResultItem;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import com.cranesvarsity.template.service.McqPracticeResultsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/student/practice/mcq-results")
public class McqPracticeResultsController {

    private final McqPracticeResultsService service;

    public McqPracticeResultsController(McqPracticeResultsService service) {
        this.service = service;
    }

    @GetMapping
    public List<McqPracticeResultItem> list(@AuthenticationPrincipal AuthenticatedStudent student) {
        return service.list(student);
    }
}
