package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.ProjectItem;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import com.cranesvarsity.template.service.ProjectException;
import com.cranesvarsity.template.service.ProjectUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/student/projects")
public class ProjectController {

    private final ProjectUploadService projectUploadService;

    public ProjectController(ProjectUploadService projectUploadService) {
        this.projectUploadService = projectUploadService;
    }

    @GetMapping
    public List<ProjectItem> list(@AuthenticationPrincipal AuthenticatedStudent student) {
        return projectUploadService.list(student);
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> upload(@AuthenticationPrincipal AuthenticatedStudent student,
                                     @RequestParam("projectTypeCode") String projectTypeCode,
                                     @RequestParam("projectTitle") String projectTitle,
                                     @RequestParam("file") MultipartFile file) {
        try {
            boolean updated = projectUploadService.upload(student, projectTypeCode, projectTitle, file);
            String message = updated ? "Project Updated Successfully!" : "Project Uploaded Successfully!";
            return ResponseEntity.ok(Map.of("success", true, "message", message));
        } catch (ProjectException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
        }
    }
}
