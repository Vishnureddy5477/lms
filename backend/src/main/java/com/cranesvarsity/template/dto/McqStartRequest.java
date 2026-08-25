package com.cranesvarsity.template.dto;

/**
 * Identifies which test the student is trying to start.
 *
 * Deliberately carries no student, batch, date or time fields: the legacy
 * validateTestAccess.jsp accepted regno, batchno, test_date, start_time and
 * end_time as request parameters and trusted them. All of those are now read
 * from the authenticated principal and the schedule row instead.
 */
public record McqStartRequest(
        String module,
        int testNo
) {
}
