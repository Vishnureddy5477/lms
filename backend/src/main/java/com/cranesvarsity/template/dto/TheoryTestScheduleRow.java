package com.cranesvarsity.template.dto;

/**
 * The raw schedule row behind one test, read back from
 * exam_system.manage_theory_tests.
 *
 * {@code questions} is the legacy comma-separated list of question-paper ids
 * ("12,13,14,15,16") — the pool one variant is drawn from per student. It is
 * only populated when a single row is fetched for the paper handout; the
 * listing query leaves it null because the browser must never see it.
 */
public record TheoryTestScheduleRow(
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
        String questions
) {
}
