package com.cranesvarsity.template.dto;

import java.math.BigDecimal;

/** One module's row on the Report Card / Marks Card — successor of legacy mark-card.jsp. */
public record ModuleMarksRow(
        int sNo,
        String moduleName,
        MarkComponent mcq,
        MarkComponent theory,
        MarkComponent lab,
        MarkComponent assignment,
        MarkComponent project,
        BigDecimal totalMax,
        BigDecimal totalObtained,
        /** False when the batch has conducted none of this module's components yet. */
        boolean scored
) {
}
