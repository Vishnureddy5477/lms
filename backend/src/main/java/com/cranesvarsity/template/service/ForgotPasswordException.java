package com.cranesvarsity.template.service;

/** Carries a user-facing message for forgot-password flow rejections, matching legacy login.jsp's step-by-step error text. */
public class ForgotPasswordException extends RuntimeException {
    public ForgotPasswordException(String message) {
        super(message);
    }
}
