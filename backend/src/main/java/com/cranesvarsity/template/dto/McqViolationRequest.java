package com.cranesvarsity.template.dto;

/**
 * A proctoring strike reported by the test screen (tab switch, window blur,
 * fullscreen exit, devtools shortcut).
 *
 * The client reports the EVENT only. The server owns the running total, writes
 * the audit row, and decides whether the limit is reached — so a refresh, a
 * reopened tab, or a tampered client cannot reset or under-report strikes.
 */
public record McqViolationRequest(
        String type,
        String reason
) {
}
