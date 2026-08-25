package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.AttendanceDetailRecord;
import com.cranesvarsity.template.dto.AttendanceSummaryItem;
import com.cranesvarsity.template.dto.McqPerformanceItem;
import com.cranesvarsity.template.dto.PlacementStatsResponse;
import com.cranesvarsity.template.dto.StudentProfileResponse;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import com.cranesvarsity.template.service.StudentDashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Backs the Dashboard/Overview page. All endpoints require a valid JWT
 * (see SecurityConfig); the student's identity comes from the token, never
 * from a request parameter.
 */
@RestController
@RequestMapping("/student")
public class StudentDashboardController {

    private final StudentDashboardService service;

    public StudentDashboardController(StudentDashboardService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public StudentProfileResponse me(@AuthenticationPrincipal AuthenticatedStudent student) {
        return service.getProfile(student);
    }

    @GetMapping("/attendance/summary")
    public List<AttendanceSummaryItem> attendanceSummary(@AuthenticationPrincipal AuthenticatedStudent student) {
        return service.getAttendanceSummary(student);
    }

    @GetMapping("/attendance/details")
    public List<AttendanceDetailRecord> attendanceDetails(@AuthenticationPrincipal AuthenticatedStudent student) {
        return service.getAttendanceDetails(student);
    }

    @GetMapping("/mcq/performance")
    public List<McqPerformanceItem> mcqPerformance(@AuthenticationPrincipal AuthenticatedStudent student) {
        return service.getMcqPerformance(student);
    }

    @GetMapping("/placement/stats")
    public PlacementStatsResponse placementStats(@AuthenticationPrincipal AuthenticatedStudent student) {
        return service.getPlacementStats(student);
    }
}
