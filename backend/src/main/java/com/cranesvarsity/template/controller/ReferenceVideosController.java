package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.ReferenceVideoItem;
import com.cranesvarsity.template.service.ReferenceVideosService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/student/resources/reference-videos")
public class ReferenceVideosController {

    private final ReferenceVideosService referenceVideosService;

    public ReferenceVideosController(ReferenceVideosService referenceVideosService) {
        this.referenceVideosService = referenceVideosService;
    }

    @GetMapping("/modules")
    public List<String> listModules() {
        return referenceVideosService.listModules();
    }

    @GetMapping
    public List<ReferenceVideoItem> listVideos(@RequestParam String module) {
        return referenceVideosService.listVideos(module);
    }
}
