package com.cranesvarsity.template.dto;

/**
 * One question-paper variant from exam_system.manage_theory_questions_pdf.
 *
 * {@code code} is the trainer-facing label ("CPP-VLSI - 14") and is safe to
 * show. {@code link} is the S3 URL and stays on the server.
 */
public record TheoryQuestionPaperFile(String link, String code) {
}
