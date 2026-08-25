package com.cranesvarsity.template.dto;

/** One module row for the Theory/Lab/Project Skill Tracker tabs — same shape across all three. */
public record SkillTrackerMarksRow(
        String module,
        int totalMarks,
        int obtainedMarks,
        String remarks
) {
}
