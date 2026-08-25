package com.cranesvarsity.template.dto;

import java.util.List;

public record CourseModulesResponse(String course, List<CourseModuleItem> modules) {
}
