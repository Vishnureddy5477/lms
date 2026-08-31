package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.dto.TheoryQuestionPaper;
import com.cranesvarsity.template.dto.TheoryTestItem;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import com.cranesvarsity.template.service.QuestionPaperProxyService;
import com.cranesvarsity.template.service.TheoryTestService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The Theory/Lab Test page.
 *
 * Note what the client never sends: registration number, batch, or the test's
 * start/end times. All of it is taken from the JWT principal and the schedule
 * row, so neither the paper handout nor the PDF stream can be pointed at
 * another student's test or talked into ignoring the clock.
 */
@RestController
@RequestMapping("/student/assessment/theory-tests")
public class TheoryTestController {

    private final TheoryTestService theoryTestService;
    private final QuestionPaperProxyService proxyService;

    public TheoryTestController(TheoryTestService theoryTestService,
                                QuestionPaperProxyService proxyService) {
        this.theoryTestService = theoryTestService;
        this.proxyService = proxyService;
    }

    /** The schedule, already bucketed into upcoming/live/ended on the server clock. */
    @GetMapping
    public List<TheoryTestItem> get(@AuthenticationPrincipal AuthenticatedStudent student) {
        return theoryTestService.getSchedule(student);
    }

    /**
     * "View Questions" — draw this student's variant of the paper, or return
     * the one they were already locked to.
     *
     * A refusal (too early, too late, wrong batch, nothing uploaded) comes back
     * as a 200 with allowed=false and a reason, so the card can explain itself.
     */
    @PostMapping("/{theoryTestId}/question-paper")
    public TheoryQuestionPaper assignQuestionPaper(@AuthenticationPrincipal AuthenticatedStudent student,
                                                   @PathVariable String theoryTestId) {
        return theoryTestService.assignQuestionPaper(student, theoryTestId);
    }

    /**
     * The PDF itself, streamed from our own origin so the storage URL stays
     * server-side and the viewer has a same-origin blob to render.
     *
     * Every guard runs again here, because this is the URL that reaches the
     * browser: it must not keep working after the test window closes.
     */
    @GetMapping("/{theoryTestId}/question-paper.pdf")
    public ResponseEntity<byte[]> streamQuestionPaper(@AuthenticationPrincipal AuthenticatedStudent student,
                                                      @PathVariable String theoryTestId) {
        byte[] pdf = proxyService.fetch(theoryTestService.resolvePdfLink(student, theoryTestId));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                // inline, and with a name that is no use as a download
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"question-paper.pdf\"")
                // never let a proxy or the browser keep a copy that outlives the window
                .header(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate, private")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .header("X-Content-Type-Options", "nosniff")
                .body(pdf);
    }
}
