package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.*;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import com.cranesvarsity.template.service.McqModuleTestService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * The MCQ Module Test engine.
 *
 * Every endpoint takes the student from {@code @AuthenticationPrincipal} and
 * the session-scoped ones verify the session belongs to that student, so
 * nothing here can be pointed at another student's attempt.
 */
@RestController
@RequestMapping("/student/assessment/mcq-test")
public class McqModuleTestController {

    private final McqModuleTestService service;

    public McqModuleTestController(McqModuleTestService service) {
        this.service = service;
    }

    /** Run the gate without starting anything, so the list page can show the real reason. */
    @PostMapping("/access-check")
    public McqAccessResult accessCheck(@AuthenticationPrincipal AuthenticatedStudent student,
                                       @RequestBody McqStartRequest request) {
        return service.accessCheck(student, request.module(), request.testNo());
    }

    /** Start the attempt, or resume the one already open for this test. */
    @PostMapping("/session/start")
    public McqSessionState start(@AuthenticationPrincipal AuthenticatedStudent student,
                                 @RequestBody McqStartRequest request) {
        return service.startOrResume(student, request.module(), request.testNo());
    }

    /** Re-read the attempt (used when the test screen is reloaded mid-test). */
    @GetMapping("/session/{sessionId}")
    public McqSessionState state(@AuthenticationPrincipal AuthenticatedStudent student,
                                 @PathVariable int sessionId) {
        return service.getState(student, sessionId);
    }

    /**
     * Persist a debounced batch of answers.
     *
     * The reply carries the authoritative clock and strike count, which is why
     * the test screen needs no separate timer poll while the student is active.
     */
    @PostMapping("/session/{sessionId}/answers")
    public McqSyncResponse saveAnswers(@AuthenticationPrincipal AuthenticatedStudent student,
                                       @PathVariable int sessionId,
                                       @RequestBody McqAnswerSave request) {
        return service.saveAnswers(student, sessionId, request);
    }

    /** Report a proctoring strike. The server owns the count, not the client. */
    @PostMapping("/session/{sessionId}/violation")
    public McqSyncResponse violation(@AuthenticationPrincipal AuthenticatedStudent student,
                                     @PathVariable int sessionId,
                                     @RequestBody McqViolationRequest request) {
        return service.reportViolation(student, sessionId, request);
    }

    /** Idle keep-alive — only fires when no answer has been saved recently. */
    @GetMapping("/session/{sessionId}/heartbeat")
    public McqSyncResponse heartbeat(@AuthenticationPrincipal AuthenticatedStudent student,
                                     @PathVariable int sessionId) {
        return service.heartbeat(student, sessionId);
    }

    /** Score and close the attempt. Safe to call more than once. */
    @PostMapping("/session/{sessionId}/submit")
    public McqSubmitResult submit(@AuthenticationPrincipal AuthenticatedStudent student,
                                  @PathVariable int sessionId,
                                  @RequestParam(defaultValue = "false") boolean auto) {
        return service.submit(student, sessionId, auto);
    }
}
