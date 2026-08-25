package com.cranesvarsity.template.dto;

/** One row of the "attendance" table — backs the full Attendance Details page. */
public record AttendanceDetailRecord(
        String module,
        String classDate,
        String presentAbsent,
        String startTime,
        String endTime,
        String trainer
) {
}
