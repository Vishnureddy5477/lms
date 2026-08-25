package com.cranesvarsity.template.dto;

/** One module's row on the Report Card / Marks Card — successor of legacy mark-card.jsp. */
public record ModuleMarksRow(
        String moduleName,
        int mcqMax,
        int mcqObtained,
        int theoryMax,
        int theoryObtained,
        int labMax,
        int labObtained,
        int assignmentMax,
        int assignmentObtained,
        int projectMax,
        int projectObtained,
        int totalMax,
        int totalObtained
) {
}
