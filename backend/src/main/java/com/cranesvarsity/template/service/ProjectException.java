package com.cranesvarsity.template.service;

/** Carries a user-facing message for project upload rejections. */
public class ProjectException extends RuntimeException {
    public ProjectException(String message) {
        super(message);
    }
}
