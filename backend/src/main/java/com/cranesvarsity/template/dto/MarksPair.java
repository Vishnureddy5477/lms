package com.cranesvarsity.template.dto;

/** Internal max/obtained pair used by the report-card mark DAOs. */
public record MarksPair(int max, int obtained) {
    public static final MarksPair ZERO = new MarksPair(0, 0);
}
