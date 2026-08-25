package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.StudyMaterialItem;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import com.cranesvarsity.template.service.StudyMaterialException;
import com.cranesvarsity.template.service.StudyMaterialsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/student/resources/study-materials")
public class StudyMaterialsController {

    private final StudyMaterialsService studyMaterialsService;

    public StudyMaterialsController(StudyMaterialsService studyMaterialsService) {
        this.studyMaterialsService = studyMaterialsService;
    }

    @GetMapping
    public List<StudyMaterialItem> list(@AuthenticationPrincipal AuthenticatedStudent student) {
        return studyMaterialsService.list(student);
    }

    @PostMapping("/{id}/download")
    public ResponseEntity<?> download(@AuthenticationPrincipal AuthenticatedStudent student, @PathVariable int id) {
        try {
            String link = studyMaterialsService.recordDownloadAndGetLink(student, id);
            return ResponseEntity.ok(Map.of("link", link));
        } catch (StudyMaterialException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
        }
    }
}
