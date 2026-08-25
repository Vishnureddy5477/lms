package com.cranesvarsity.template.dto;

/**
 * The reply to every answer-save and every reported violation.
 *
 * Both carry the authoritative clock and strike count back, which is why the
 * test screen needs no separate timer poll while the student is active — the
 * legacy panel spent ~120 requests per test on that alone. An idle heartbeat
 * only fires when nothing else has been sent recently.
 *
 * {@code shouldAutoSubmit} is decided by the SERVER (time expired, or strikes
 * reached the limit). The client obeys it; it never makes that call itself.
 */
public record McqSyncResponse(
        long remainingSeconds,
        int violationCount,
        int maxViolations,
        boolean shouldAutoSubmit,
        String reason
) {
}
