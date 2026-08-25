package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.ScheduleResponse;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import com.cranesvarsity.template.service.ScheduleService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/student/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public ScheduleResponse get(@AuthenticationPrincipal AuthenticatedStudent student) {
        return scheduleService.getSchedule(student);
    }
}
