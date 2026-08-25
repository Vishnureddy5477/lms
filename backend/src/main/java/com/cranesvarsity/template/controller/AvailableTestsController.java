package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.AvailableTestItem;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import com.cranesvarsity.template.service.AvailableTestsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/student/assessment/available-tests")
public class AvailableTestsController {

    private final AvailableTestsService availableTestsService;

    public AvailableTestsController(AvailableTestsService availableTestsService) {
        this.availableTestsService = availableTestsService;
    }

    @GetMapping
    public List<AvailableTestItem> get(@AuthenticationPrincipal AuthenticatedStudent student) {
        return availableTestsService.getAvailableTests(student);
    }
}
