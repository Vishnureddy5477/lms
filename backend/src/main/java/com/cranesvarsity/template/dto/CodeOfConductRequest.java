package com.cranesvarsity.template.dto;

/**
 * Mirrors the Code of Conduct form. projectCertificate is only meaningful
 * when interested="No" (matches legacy placement-code-of-conduct.jsp).
 */
public record CodeOfConductRequest(
        String interested,
        String projectCertificate,
        String message,
        boolean acceptance,
        String signature
) {
}
