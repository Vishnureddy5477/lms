package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.EvaluationPolicyResponse;
import com.cranesvarsity.template.service.EvaluationPolicyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/student/evaluation-policy")
public class EvaluationPolicyController {

    private final EvaluationPolicyService evaluationPolicyService;

    public EvaluationPolicyController(EvaluationPolicyService evaluationPolicyService) {
        this.evaluationPolicyService = evaluationPolicyService;
    }

    @GetMapping
    public EvaluationPolicyResponse get() {
        return evaluationPolicyService.getPolicy();
    }
}
