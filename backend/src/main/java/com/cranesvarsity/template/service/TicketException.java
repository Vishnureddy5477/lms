package com.cranesvarsity.template.service;

/** Carries a user-facing message for ticket submission rejections. */
public class TicketException extends RuntimeException {
    public TicketException(String message) {
        super(message);
    }
}
