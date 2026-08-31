package com.cranesvarsity.template.dto;

/**
 * One row of the Theory/Lab Test schedule — successor of legacy
 * theory_test_schedule.jsp.
 *
 * The legacy page shipped only the raw date/time strings and let the browser
 * decide whether a test was upcoming, live or ended — which meant a student
 * with a wrong (or deliberately shifted) system clock saw the wrong state.
 * {@code status} and the two second counts below are computed on the server in
 * IST, so the card only ever counts down from a number the server handed it.
 */
public record TheoryTestItem(
        String theoryTestId,
        String domainName,
        String testType,
        String moduleName,
        String batchName,
        String testDate,
        String testStartTime,
        String testEndTime,
        String totalMarks,
        String totalQuestions,
        /** "upcoming" | "live" | "ended" */
        String status,
        /** Seconds until the window opens; 0 once it has. */
        long secondsUntilStart,
        /** Seconds until the window closes; 0 once it has. */
        long secondsRemaining,
        /** Full length of the window, so the card can draw a progress bar. */
        long durationSeconds
) {
}
