package com.cranesvarsity.template.service;

/** Carries a user-facing message for feedback submission rejections. */
public class FeedbackException extends RuntimeException {
    public FeedbackException(String message) {
        super(message);
    }
}
