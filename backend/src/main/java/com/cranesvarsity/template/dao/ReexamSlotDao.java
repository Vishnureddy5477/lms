package com.cranesvarsity.template.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Paid re-exam entitlements — "cranescrm.reexam_payment_mapping".
 *
 * The business rule is unchanged from the legacy portal: the first two attempts
 * at a module test are free; from the third on, the student needs an unused
 * paid slot for that exact module + test + batch, and one slot is consumed per
 * attempt taken.
 */
@Repository
public class ReexamSlotDao {

    private final JdbcTemplate jdbcTemplate;

    public ReexamSlotDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Is there an unused paid slot covering this exact test? */
    public boolean hasUnusedSlot(String regNo, String module, int testNo, String batch) {
        Integer n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cranescrm.reexam_payment_mapping " +
                        "WHERE reg_no = ? AND module = ? AND no_of_test = ? AND batchno = ? AND is_used = 0",
                Integer.class, regNo, module, String.valueOf(testNo), batch);
        return n != null && n > 0;
    }

    /**
     * Consume the oldest unused slot. Called only after a failed attempt 3+.
     *
     * Non-fatal by design: the result is already saved by the time this runs, so
     * a failure here is logged rather than allowed to roll back a real score.
     */
    public int consumeOldestSlot(String regNo, String module, int testNo, String batch) {
        return jdbcTemplate.update(
                "UPDATE cranescrm.reexam_payment_mapping " +
                        "SET is_used = 1, used_at = NOW() " +
                        "WHERE reg_no = ? AND module = ? AND no_of_test = ? AND batchno = ? AND is_used = 0 " +
                        "ORDER BY created_at ASC LIMIT 1",
                regNo, module, String.valueOf(testNo), batch);
    }
}
