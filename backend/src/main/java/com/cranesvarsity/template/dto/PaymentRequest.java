package com.cranesvarsity.template.dto;

/**
 * contact/batch/regno/name/email are intentionally NOT part of this request —
 * they are resolved server-side from the authenticated student's admission
 * record so a client can't submit a payment under someone else's identity.
 */
public record PaymentRequest(String paymentMethod, String amount, String date, String remarks) {
}
