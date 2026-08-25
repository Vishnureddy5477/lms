package com.cranesvarsity.template.service;

/** Carries a user-facing message for document upload/delete rejections. */
public class DocumentException extends RuntimeException {
    public DocumentException(String message) {
        super(message);
    }
}
