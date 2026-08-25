package com.cranesvarsity.template.dto;

/** One row of "Today's Live Classes" — successor of legacy my-schedule.jsp's top section. */
public record TodayClassItem(
        String module,
        String trainer,
        String startTime,
        String endTime,
        String status,
        boolean linkActive,
        String joinLink
) {
}
