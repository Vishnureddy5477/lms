package com.cranesvarsity.template.dto;

public record FeedbackSubmitRequest(String batch, String module, String trainer, FeedbackFormData form) {
}
