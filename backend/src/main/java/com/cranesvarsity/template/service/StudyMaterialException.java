package com.cranesvarsity.template.service;

/** Carries a user-facing message for study material lookup failures. */
public class StudyMaterialException extends RuntimeException {
    public StudyMaterialException(String message) {
        super(message);
    }
}
