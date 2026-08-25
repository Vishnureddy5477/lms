package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.CompanyJobsResponse;
import com.cranesvarsity.template.dto.JobInterestRequest;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import com.cranesvarsity.template.service.CompanyJobService;
import com.cranesvarsity.template.service.PlacementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/student/placement/jobs")
public class CompanyJobController {

    private final CompanyJobService companyJobService;

    public CompanyJobController(CompanyJobService companyJobService) {
        this.companyJobService = companyJobService;
    }

    @GetMapping
    public CompanyJobsResponse get(@AuthenticationPrincipal AuthenticatedStudent student) {
        return companyJobService.getOpenJobs(student);
    }

    @PostMapping("/{id}/interest")
    public ResponseEntity<?> submitInterest(@AuthenticationPrincipal AuthenticatedStudent student,
                                             @PathVariable long id,
                                             @RequestBody JobInterestRequest request) {
        try {
            companyJobService.submitInterest(student, id, request.interested());
            return ResponseEntity.ok(Map.of("success", true, "message", "You have applied successfully!"));
        } catch (PlacementException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
        }
    }
}
