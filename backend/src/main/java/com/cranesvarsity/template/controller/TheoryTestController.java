package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.TheoryTestItem;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import com.cranesvarsity.template.service.TheoryTestService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/student/assessment/theory-tests")
public class TheoryTestController {

    private final TheoryTestService theoryTestService;

    public TheoryTestController(TheoryTestService theoryTestService) {
        this.theoryTestService = theoryTestService;
    }

    @GetMapping
    public List<TheoryTestItem> get(@AuthenticationPrincipal AuthenticatedStudent student) {
        return theoryTestService.getSchedule(student);
    }
}
