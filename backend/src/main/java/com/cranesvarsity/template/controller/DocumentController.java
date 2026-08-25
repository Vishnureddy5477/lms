package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.DocumentItem;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import com.cranesvarsity.template.service.DocumentException;
import com.cranesvarsity.template.service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/student/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    public List<DocumentItem> list(@AuthenticationPrincipal AuthenticatedStudent student) {
        return documentService.list(student);
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> upload(@AuthenticationPrincipal AuthenticatedStudent student,
                                     @RequestParam("docType") String docType,
                                     @RequestParam("file") MultipartFile file) {
        try {
            documentService.upload(student, docType, file);
            return ResponseEntity.ok(Map.of("success", true, "message", "Document uploaded successfully."));
        } catch (DocumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@AuthenticationPrincipal AuthenticatedStudent student, @PathVariable int id) {
        try {
            documentService.delete(student, id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Record deleted successfully."));
        } catch (DocumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
        }
    }
}
