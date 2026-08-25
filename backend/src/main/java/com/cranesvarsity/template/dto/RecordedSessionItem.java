package com.cranesvarsity.template.dto;

public record RecordedSessionItem(
        int slNo,
        String batch,
        String subject,
        String recordedLink,
        String date,
        String status
) {
}
