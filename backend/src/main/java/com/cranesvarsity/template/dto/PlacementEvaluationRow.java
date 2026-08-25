package com.cranesvarsity.template.dto;

/** One row of the Placement Evaluation tab — global reference table, not student-scoped. */
public record PlacementEvaluationRow(
        int slNo, String types, String testName, String modes, String marks, String total, String passMarks,
        String updateOn, String updatedBy
) {
}
