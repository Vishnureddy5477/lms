package com.cranesvarsity.template.dto;

/** One row of the Theory/Lab Test schedule — successor of legacy theory_test_schedule.jsp. */
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
        String totalQuestions
) {
}
