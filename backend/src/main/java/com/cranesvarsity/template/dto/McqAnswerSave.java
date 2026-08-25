package com.cranesvarsity.template.dto;

import java.util.List;

/**
 * A debounced batch of answers from the test screen.
 *
 * The legacy panel fired one request per click (~180-220 per test once
 * NOT_ATTEMPTED saves and timer polls are counted). The client now coalesces
 * clicks over a short debounce window and sends them together, which keeps the
 * save-as-you-go guarantee (at most one click is ever in flight) while cutting
 * the request count by roughly an order of magnitude.
 *
 * Note there is no correctness field — the client does not get a say.
 */
public record McqAnswerSave(
        List<Item> answers
) {
    /** {@code selectedOption}: 0 = not attempted / cleared, 1..4 = chosen option. */
    public record Item(int questionOrder, int selectedOption) {
    }
}
