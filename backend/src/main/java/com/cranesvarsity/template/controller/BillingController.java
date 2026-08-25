package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.BillingHistoryResponse;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import com.cranesvarsity.template.service.BillingService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/student/billing")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping("/history")
    public BillingHistoryResponse history(@AuthenticationPrincipal AuthenticatedStudent student) {
        return billingService.getHistory(student);
    }
}
