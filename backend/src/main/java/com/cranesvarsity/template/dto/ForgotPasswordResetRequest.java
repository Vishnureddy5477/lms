package com.cranesvarsity.template.dto;

public record ForgotPasswordResetRequest(String email, String newPassword, String confirmPassword) {
}
