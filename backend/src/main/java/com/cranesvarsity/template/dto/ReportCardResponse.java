package com.cranesvarsity.template.dto;

import java.util.List;

public record ReportCardResponse(String course, List<ModuleMarksRow> modules) {
}
