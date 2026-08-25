package com.cranesvarsity.template.dto;

public record AttendanceSummaryItem(
        String moduleName,
        int totalClasses,
        int presentCount,
        int absentCount,
        double averagePercent
) {
}
