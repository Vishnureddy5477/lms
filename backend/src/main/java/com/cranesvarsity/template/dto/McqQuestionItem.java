package com.cranesvarsity.template.dto;

/**
 * One question slot as the student sees it.
 *
 * Deliberately has NO correct-answer field: the answer key never leaves the
 * server. Correctness is decided server-side on every save (see
 * McqModuleTestService#saveAnswers).
 *
 * {@code questionOrder} — not questionId — is the identity the client sends
 * back when answering. A thin question bank may have to backfill a duplicate
 * question to fill 40 slots, so the same questionId can occupy two slots and
 * each must be answerable independently.
 */
public record McqQuestionItem(
        int questionOrder,
        int questionId,
        String question,
        String opt1,
        String opt2,
        String opt3,
        String opt4
) {
}
