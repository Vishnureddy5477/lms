package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.PlacementMonitorResponse;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import com.cranesvarsity.template.service.PlacementMonitorService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/student/placement/monitor")
public class PlacementMonitorController {

    private final PlacementMonitorService placementMonitorService;

    public PlacementMonitorController(PlacementMonitorService placementMonitorService) {
        this.placementMonitorService = placementMonitorService;
    }

    @GetMapping
    public PlacementMonitorResponse get(@AuthenticationPrincipal AuthenticatedStudent student) {
        return placementMonitorService.getMonitor(student);
    }
}
