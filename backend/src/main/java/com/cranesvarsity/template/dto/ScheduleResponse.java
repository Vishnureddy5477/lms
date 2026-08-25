package com.cranesvarsity.template.dto;

import java.util.List;

public record ScheduleResponse(List<TodayClassItem> todayClasses, List<WeekClassItem> weekClasses) {
}
