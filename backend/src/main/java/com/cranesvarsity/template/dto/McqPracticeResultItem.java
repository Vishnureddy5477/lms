package com.cranesvarsity.template.dto;

public record McqPracticeResultItem(
        int slNo,
        String registrationNo,
        String moduleName,
        String chapter,
        int totalCorrect,
        int totalIncorrect,
        int totalNotAttempt,
        String percentage,
        String examDatetime
) {
}
