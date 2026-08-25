package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.InterviewAnswerItem;
import com.cranesvarsity.template.dto.InterviewCompanyModuleItem;
import com.cranesvarsity.template.dto.InterviewQuestionItem;
import com.cranesvarsity.template.service.InterviewQuestionsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/student/practice/interview-questions")
public class InterviewQuestionsController {

    private final InterviewQuestionsService service;

    public InterviewQuestionsController(InterviewQuestionsService service) {
        this.service = service;
    }

    @GetMapping("/companies")
    public List<InterviewCompanyModuleItem> listCompanyModules() {
        return service.listCompanyModules();
    }

    @GetMapping
    public List<InterviewQuestionItem> listQuestions(@RequestParam String moduleCategory) {
        return service.listQuestions(moduleCategory);
    }

    @GetMapping("/{questionId}/answers")
    public List<InterviewAnswerItem> listAnswers(@PathVariable int questionId) {
        return service.listAnswers(questionId);
    }
}
