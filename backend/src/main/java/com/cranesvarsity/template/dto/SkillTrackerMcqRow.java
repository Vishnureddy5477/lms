package com.cranesvarsity.template.dto;

/** One attempt row from exam_system.moduletestresult — copied faithfully from student-performance.jsp. */
public record SkillTrackerMcqRow(
        String module,
        String test,
        String total,
        String obtained,
        String rate,
        String status,
        String examDate,
        String nextExamDate,
        int attempt
) {
}
