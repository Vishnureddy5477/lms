package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.DlModuleContent;
import com.cranesvarsity.template.dto.DlModuleItem;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import com.cranesvarsity.template.service.DigitalContentService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/student/resources/digital-content")
public class DigitalContentController {

    private final DigitalContentService digitalContentService;

    public DigitalContentController(DigitalContentService digitalContentService) {
        this.digitalContentService = digitalContentService;
    }

    @GetMapping("/modules")
    public List<DlModuleItem> listModules(@AuthenticationPrincipal AuthenticatedStudent student) {
        return digitalContentService.listModules(student);
    }

    @GetMapping("/modules/{moduleId}")
    public DlModuleContent getModuleContent(@PathVariable int moduleId) {
        return digitalContentService.getModuleContent(moduleId);
    }
}
