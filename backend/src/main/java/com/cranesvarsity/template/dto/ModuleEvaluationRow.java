package com.cranesvarsity.template.dto;

/** One row of the Module Evaluation tab — global reference table, not student-scoped. */
public record ModuleEvaluationRow(
        int slNo, String types, String modes, String marks, String total, String passMarks,
        String updateOn, String updatedBy
) {
}
