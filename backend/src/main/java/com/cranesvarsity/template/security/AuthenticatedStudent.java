package com.cranesvarsity.template.security;

/**
 * The JWT principal set on the SecurityContext for every authenticated
 * request. Controllers pull this via @AuthenticationPrincipal instead of
 * trusting any student-identifying fields from the request body.
 */
public record AuthenticatedStudent(String email, String regNo, String name, String batch) {
}
