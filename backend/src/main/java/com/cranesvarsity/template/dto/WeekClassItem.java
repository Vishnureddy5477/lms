package com.cranesvarsity.template.dto;

/** One row of "This Week's Schedule" — successor of legacy my-schedule.jsp's bottom section. */
public record WeekClassItem(
        String module,
        String trainer,
        String startTime,
        String endTime,
        String startDate,
        String endDate,
        String mode,
        String status
) {
}
