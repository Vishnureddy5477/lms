package com.cranesvarsity.template.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * Module-name keys and mark values, shared by the report-card DAOs.
 *
 * Both exist because the marks tables are free text. Module names are
 * inconsistently cased across them — attendance writes "IOT", moduletestresult
 * writes "IoT" — so matching case-sensitively silently drops real marks. Mark
 * columns are varchar(200) holding things like "16.5" and
 * "22.000000000000004", so they are parsed tolerantly rather than read with
 * getInt(), which cannot represent a half mark at all.
 */
public final class Marks {

    private Marks() {
    }

    /** Lower-cased, trimmed module name — the only key the report card matches on. */
    public static String key(String moduleName) {
        return moduleName == null ? "" : moduleName.trim().toLowerCase(Locale.ROOT);
    }

    /** Tolerant parse: blank, null and non-numeric all read as zero rather than throwing. */
    public static BigDecimal value(String raw) {
        if (raw == null) {
            return BigDecimal.ZERO;
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            BigDecimal v = new BigDecimal(s).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
            return v.scale() < 0 ? v.setScale(0) : v;
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
