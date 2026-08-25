package com.cranesvarsity.template.service;

/** Carries a user-facing message for placement-related rejections (already applied, etc). */
public class PlacementException extends RuntimeException {
    public PlacementException(String message) {
        super(message);
    }
}
