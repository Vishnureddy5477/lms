package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.EnrollmentSubmissionRequest;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import com.cranesvarsity.template.service.EnrollmentException;
import com.cranesvarsity.template.service.EnrollmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/student/enrollment")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/status")
    public Map<String, Boolean> status(@AuthenticationPrincipal AuthenticatedStudent student) {
        return Map.of("submitted", enrollmentService.isSubmitted(student));
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> submit(@AuthenticationPrincipal AuthenticatedStudent student,
                                     @RequestPart("data") EnrollmentSubmissionRequest data,
                                     @RequestPart("photo") MultipartFile photo) {
        try {
            enrollmentService.submit(student, data, photo);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Enrollment form is submitted. Thank you."
            ));
        } catch (EnrollmentException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
        }
    }
}
