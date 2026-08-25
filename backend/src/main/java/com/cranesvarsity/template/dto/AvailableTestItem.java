package com.cranesvarsity.template.dto;

/** One row of the Available MCQ Tests list — successor of legacy getAvailableTests.jsp. */
public record AvailableTestItem(
        String id,
        String moduleName,
        String batchName,
        String mcqTestNo,
        String testDate,
        String testStartTime,
        String testEndTime,
        String testType
) {
}
