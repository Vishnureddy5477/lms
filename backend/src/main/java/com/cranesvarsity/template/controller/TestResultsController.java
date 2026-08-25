package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.TestResultsResponse;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import com.cranesvarsity.template.service.TestResultsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/student/test-results")
public class TestResultsController {

    private final TestResultsService testResultsService;

    public TestResultsController(TestResultsService testResultsService) {
        this.testResultsService = testResultsService;
    }

    @GetMapping("/modules")
    public List<String> getModules(@AuthenticationPrincipal AuthenticatedStudent student) {
        return testResultsService.getModules(student);
    }

    @GetMapping
    public TestResultsResponse getResults(@AuthenticationPrincipal AuthenticatedStudent student,
                                           @RequestParam String subject) {
        return testResultsService.getResults(student, subject);
    }
}
