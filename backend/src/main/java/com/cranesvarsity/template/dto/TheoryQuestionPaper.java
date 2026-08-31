package com.cranesvarsity.template.dto;

/**
 * The reply to "View Questions" on the Theory/Lab Test page.
 *
 * Shaped like {@link McqAccessResult}: a refusal is a normal 200 carrying the
 * reason, so the card can explain *why* it will not open the paper instead of
 * surfacing a bare HTTP error.
 *
 * Note what is NOT here: the real storage URL of the PDF. {@code fileUrl} is a
 * path back into this API, which streams the bytes after re-running the same
 * checks — the student never learns where the file actually lives.
 */
public record TheoryQuestionPaper(
        boolean allowed,
        String title,
        String message,
        String theoryTestId,
        String testType,
        String moduleName,
        /** Which variant of the paper this student is locked to, for invigilation. */
        String questionPaperId,
        /** API path that streams the PDF; null when {@code allowed} is false. */
        String fileUrl,
        /** Server-authoritative seconds left in the test window. */
        long secondsRemaining
) {
    public static TheoryQuestionPaper block(String title, String message) {
        return new TheoryQuestionPaper(false, title, message, null, null, null, null, null, 0);
    }

    public static TheoryQuestionPaper allow(String theoryTestId, String testType, String moduleName,
                                            String questionPaperId, String fileUrl, long secondsRemaining) {
        return new TheoryQuestionPaper(true, null, null, theoryTestId, testType, moduleName,
                questionPaperId, fileUrl, secondsRemaining);
    }
}
