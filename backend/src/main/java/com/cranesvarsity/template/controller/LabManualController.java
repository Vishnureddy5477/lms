package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.CourseModulesResponse;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import com.cranesvarsity.template.service.LabManualService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/student/lab-manual")
public class LabManualController {

    private final LabManualService labManualService;

    public LabManualController(LabManualService labManualService) {
        this.labManualService = labManualService;
    }

    @GetMapping
    public CourseModulesResponse get(@AuthenticationPrincipal AuthenticatedStudent student) {
        return labManualService.getLabManual(student);
    }
}
