package com.cranesvarsity.template.service;

/** Carries a user-facing message for any login rejection (bad credentials, dues, dropout, etc). */
public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}
