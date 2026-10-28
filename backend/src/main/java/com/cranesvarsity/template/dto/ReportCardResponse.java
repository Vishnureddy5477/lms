package com.cranesvarsity.template.dto;

import java.util.List;

public record ReportCardResponse(String course, String batch,
                                  List<ModuleMarksRow> modules, MarkTotals totals) {
}
