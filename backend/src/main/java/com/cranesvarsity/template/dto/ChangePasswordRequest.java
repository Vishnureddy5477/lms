package com.cranesvarsity.template.dto;

public record ChangePasswordRequest(String oldPassword, String newPassword, String confirmPassword) {
}
