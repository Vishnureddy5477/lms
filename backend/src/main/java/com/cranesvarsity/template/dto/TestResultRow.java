package com.cranesvarsity.template.dto;

/** One row of the MCQ Test Result Details page — schema exam_system.moduletestanswer. */
public record TestResultRow(
        String examId,
        String subject,
        String noOfTest,
        String test,
        String question,
        String answer,
        String answerOption,
        String correctAnswer,
        String status,
        String attempt,
        String datetime
) {
}
