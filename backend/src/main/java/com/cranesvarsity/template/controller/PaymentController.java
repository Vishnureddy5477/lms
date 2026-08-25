package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.PaymentRequest;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import com.cranesvarsity.template.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/student")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments")
    public ResponseEntity<?> submitPayment(@AuthenticationPrincipal AuthenticatedStudent student,
                                            @RequestBody PaymentRequest request) {
        paymentService.submitPayment(student, request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Submit your payment details for verification. Once the accounts team approves, you will receive a confirmation email."
        ));
    }
}
