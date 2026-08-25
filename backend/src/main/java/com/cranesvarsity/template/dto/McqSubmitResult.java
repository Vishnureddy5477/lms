package com.cranesvarsity.template.dto;

/**
 * The results summary shown after submission — successor of the scoring half of
 * legacy manage-mcq-test-submit.jsp.
 *
 * Scoring rules are unchanged from the legacy portal: 0.5 marks per correct
 * answer over 40 questions (20 total), 50% to pass, and a retake auto-scheduled
 * two days out ONLY when a first attempt fails. Attempts 2+ that fail fall
 * through to the paid re-exam slot rule instead, which is what
 * {@code paidSlotRequired} tells the student.
 */
public record McqSubmitResult(
        String status,
        int correct,
        int incorrect,
        int notAttempted,
        double obtainedMarks,
        double totalMarks,
        String percentage,
        int attemptNo,
        String nextDate,
        boolean paidSlotRequired,
        boolean autoSubmitted,
        String moduleName,
        int testNo,
        String studentName,
        String regNo,
        String batch
) {
}
