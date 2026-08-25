package com.cranesvarsity.template.dto;

public record McqPerformanceItem(
        String moduleName,
        int totalQuestions,
        double correctCount,
        double incorrectCount,
        String averagePercent,
        String status
) {
}
