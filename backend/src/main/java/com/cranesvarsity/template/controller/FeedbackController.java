package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.*;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import com.cranesvarsity.template.service.FeedbackException;
import com.cranesvarsity.template.service.FeedbackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/student/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping("/pending")
    public List<FeedbackPendingItem> getPending(@AuthenticationPrincipal AuthenticatedStudent student) {
        return feedbackService.getPendingFeedback(student);
    }

    @GetMapping("/history")
    public List<FeedbackHistoryItem> getHistory(@AuthenticationPrincipal AuthenticatedStudent student) {
        return feedbackService.getHistory(student);
    }

    @GetMapping("/form")
    public FeedbackFormData getForm(@AuthenticationPrincipal AuthenticatedStudent student,
                                     @RequestParam String batch,
                                     @RequestParam String module,
                                     @RequestParam String trainer) {
        return feedbackService.getForm(student, batch, module, trainer);
    }

    @PostMapping("/form")
    public ResponseEntity<?> submit(@AuthenticationPrincipal AuthenticatedStudent student,
                                     @RequestBody FeedbackSubmitRequest request) {
        try {
            feedbackService.submit(student, request);
            return ResponseEntity.ok(Map.of("success", true, "message", "Feedback saved successfully!"));
        } catch (FeedbackException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
        }
    }
}
