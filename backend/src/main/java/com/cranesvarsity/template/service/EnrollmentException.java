package com.cranesvarsity.template.service;

/** Carries a user-facing message for enrollment submission rejections (already submitted, etc). */
public class EnrollmentException extends RuntimeException {
    public EnrollmentException(String message) {
        super(message);
    }
}
