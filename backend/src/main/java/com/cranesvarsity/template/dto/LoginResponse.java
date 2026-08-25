package com.cranesvarsity.template.dto;

public record LoginResponse(String token, String name, String email, String regNo, String batch, String course) {
}
