package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.ClassNoteItem;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import com.cranesvarsity.template.service.ClassNotesService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/student/resources/class-notes")
public class ClassNotesController {

    private final ClassNotesService classNotesService;

    public ClassNotesController(ClassNotesService classNotesService) {
        this.classNotesService = classNotesService;
    }

    @GetMapping
    public List<ClassNoteItem> list(@AuthenticationPrincipal AuthenticatedStudent student) {
        return classNotesService.list(student);
    }
}
