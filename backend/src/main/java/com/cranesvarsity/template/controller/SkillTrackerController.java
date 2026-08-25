package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.SkillTrackerResponse;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import com.cranesvarsity.template.service.SkillTrackerService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/student/skill-tracker")
public class SkillTrackerController {

    private final SkillTrackerService skillTrackerService;

    public SkillTrackerController(SkillTrackerService skillTrackerService) {
        this.skillTrackerService = skillTrackerService;
    }

    @GetMapping
    public SkillTrackerResponse get(@AuthenticationPrincipal AuthenticatedStudent student) {
        return skillTrackerService.getSkillTracker(student);
    }
}
