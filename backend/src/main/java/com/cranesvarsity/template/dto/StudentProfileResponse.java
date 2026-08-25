package com.cranesvarsity.template.dto;

import java.time.LocalDate;

public record StudentProfileResponse(
        String name,
        String email,
        String regNo,
        String contact,
        String batch,
        String course,
        LocalDate courseStartDate,
        Double nextDues
) {
}
