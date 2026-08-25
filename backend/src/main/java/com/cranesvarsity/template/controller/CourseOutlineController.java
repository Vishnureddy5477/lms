package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.CourseModulesResponse;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import com.cranesvarsity.template.service.CourseOutlineService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/student/course-outline")
public class CourseOutlineController {

    private final CourseOutlineService courseOutlineService;

    public CourseOutlineController(CourseOutlineService courseOutlineService) {
        this.courseOutlineService = courseOutlineService;
    }

    @GetMapping
    public CourseModulesResponse get(@AuthenticationPrincipal AuthenticatedStudent student) {
        return courseOutlineService.getOutline(student);
    }
}
