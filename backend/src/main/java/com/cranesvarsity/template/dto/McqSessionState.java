package com.cranesvarsity.template.dto;

import java.util.List;
import java.util.Map;

/**
 * Everything the test screen needs to render or RESUME an attempt.
 *
 * Returned by both "start" and "resume" — they are the same call. If an open
 * session already exists for this module + test number it is reused, so the
 * student gets back the identical 40 questions in the identical order with
 * their saved answers restored and the timer where they left it.
 *
 * {@code remainingSeconds} and {@code violationCount} are computed server-side;
 * the browser clock and any client-held strike count are never trusted.
 */
public record McqSessionState(
        int sessionId,
        String moduleName,
        int testNo,
        int attemptNo,
        String studentName,
        String regNo,
        String batch,
        int totalQuestions,
        double totalMarks,
        long remainingSeconds,
        int violationCount,
        int maxViolations,
        List<McqQuestionItem> questions,
        /** questionOrder -> selected option (0 = not attempted, 1..4) */
        Map<Integer, Integer> answers
) {
}
