package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.CourseProgressRow;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import com.cranesvarsity.template.service.CourseProgressService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/student/course-progress")
public class CourseProgressController {

    private final CourseProgressService courseProgressService;

    public CourseProgressController(CourseProgressService courseProgressService) {
        this.courseProgressService = courseProgressService;
    }

    @GetMapping
    public List<CourseProgressRow> get(@AuthenticationPrincipal AuthenticatedStudent student) {
        return courseProgressService.getProgress(student);
    }
}
