package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.CodeOfConductRequest;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import com.cranesvarsity.template.service.CodeOfConductService;
import com.cranesvarsity.template.service.PlacementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/student/placement/code-of-conduct")
public class CodeOfConductController {

    private final CodeOfConductService codeOfConductService;

    public CodeOfConductController(CodeOfConductService codeOfConductService) {
        this.codeOfConductService = codeOfConductService;
    }

    @GetMapping("/status")
    public Map<String, Boolean> status(@AuthenticationPrincipal AuthenticatedStudent student) {
        return Map.of("submitted", codeOfConductService.isSubmitted(student));
    }

    @PostMapping
    public ResponseEntity<?> submit(@AuthenticationPrincipal AuthenticatedStudent student,
                                     @RequestBody CodeOfConductRequest request) {
        try {
            codeOfConductService.submit(student, request);
            return ResponseEntity.ok(Map.of("success", true, "message", "Your code of conduct is accepted, Thank you."));
        } catch (PlacementException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
        }
    }
}
