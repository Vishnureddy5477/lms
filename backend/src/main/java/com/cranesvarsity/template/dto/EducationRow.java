package com.cranesvarsity.template.dto;

/** One row of the 5-stage education table (PG / Degree / Diploma / PUC / 10th). */
public record EducationRow(
        String degree,
        String college,
        String university,
        String stream,
        String passoutYear,
        String percentage,
        String gap,
        String gapYears
) {
}
