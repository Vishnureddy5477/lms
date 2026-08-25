package com.cranesvarsity.template.dto;

/** One row of the Course Progress Status page — successor of legacy current-status.jsp. */
public record CourseProgressRow(String moduleName, String startDate, String endDate, int totalDays, String status) {
}
