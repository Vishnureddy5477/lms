package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.ReportCardResponse;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import com.cranesvarsity.template.service.ReportCardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/student/report-card")
public class ReportCardController {

    private final ReportCardService reportCardService;

    public ReportCardController(ReportCardService reportCardService) {
        this.reportCardService = reportCardService;
    }

    @GetMapping
    public ReportCardResponse get(@AuthenticationPrincipal AuthenticatedStudent student) {
        return reportCardService.getReportCard(student);
    }
}
