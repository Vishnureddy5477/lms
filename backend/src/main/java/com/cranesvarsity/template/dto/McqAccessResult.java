package com.cranesvarsity.template.dto;

/**
 * Outcome of the pre-start access gate — successor of legacy validateTestAccess.jsp.
 *
 * Each blocking check returns its own title + message so the student is told
 * exactly what stopped them (too early, too late, already passed, payment
 * required, bank incomplete) rather than one generic "access denied".
 */
public record McqAccessResult(
        boolean allowed,
        String title,
        String message
) {
    public static McqAccessResult allow() {
        return new McqAccessResult(true, "Access granted", "Starting your test…");
    }

    public static McqAccessResult block(String title, String message) {
        return new McqAccessResult(false, title, message);
    }
}
