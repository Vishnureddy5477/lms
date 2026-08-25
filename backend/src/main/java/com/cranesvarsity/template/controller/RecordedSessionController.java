package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.RecordedSessionItem;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import com.cranesvarsity.template.service.RecordedSessionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/student/tutorials")
public class RecordedSessionController {

    private final RecordedSessionService recordedSessionService;

    public RecordedSessionController(RecordedSessionService recordedSessionService) {
        this.recordedSessionService = recordedSessionService;
    }

    @GetMapping("/recorded-sessions")
    public List<RecordedSessionItem> getRecordedSessions(@AuthenticationPrincipal AuthenticatedStudent student) {
        return recordedSessionService.getRecordedSessions(student);
    }
}
