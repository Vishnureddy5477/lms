package com.cranesvarsity.template.dto;

import java.math.BigDecimal;

/**
 * One of the five mark components of a module row.
 *
 * {@code conducted} is whether the batch ran this component at all — when it
 * did not, the component contributes nothing to either side of the total and
 * the page shows a dash instead of a zero. {@code recorded} is whether this
 * student has a mark row for it, so a genuine zero can be told apart from a
 * mark nobody has entered yet.
 */
public record MarkComponent(boolean conducted, boolean recorded, BigDecimal max, BigDecimal obtained) {

    public static final MarkComponent NOT_CONDUCTED = new MarkComponent(false, false, null, null);

    public static MarkComponent of(boolean conducted, BigDecimal max, BigDecimal obtained) {
        if (!conducted) {
            return NOT_CONDUCTED;
        }
        return new MarkComponent(true, obtained != null, max, obtained == null ? BigDecimal.ZERO : obtained);
    }
}
