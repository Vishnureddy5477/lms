package com.cranesvarsity.template.dto;

import java.math.BigDecimal;

/**
 * Report-card footer. Computed here rather than summed in the browser: a
 * component the batch never conducted has no max, and adding it as zero on the
 * way past would shrink the denominator and inflate the percentage.
 */
public record MarkTotals(
        BigDecimal mcqMax, BigDecimal mcqObtained,
        BigDecimal theoryMax, BigDecimal theoryObtained,
        BigDecimal labMax, BigDecimal labObtained,
        BigDecimal assignmentMax, BigDecimal assignmentObtained,
        BigDecimal projectMax, BigDecimal projectObtained,
        BigDecimal totalMax, BigDecimal totalObtained,
        /** Null until at least one component has been conducted. */
        BigDecimal percentage,
        /** Null alongside a null percentage, so the page keeps its blank placeholder. */
        String grade
) {
}
