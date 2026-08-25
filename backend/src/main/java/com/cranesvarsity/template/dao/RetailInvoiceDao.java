package com.cranesvarsity.template.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Raw SQL against the legacy "retailinvoice" table (schema cranescrm).
 * A JdbcTemplate DAO (not a JPA entity) because the table has no single
 * natural key we can confirm from the legacy source — it's queried purely
 * for scalar reads here, exactly like login.jsp/header.jsp do.
 */
@Repository
public class RetailInvoiceDao {

    private final JdbcTemplate jdbcTemplate;

    public RetailInvoiceDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Mirrors login.jsp's dues gate: any row with overdue access + outstanding dues blocks login. */
    public boolean hasOverdueDues(String regNo) {
        String sql = "SELECT COUNT(*) FROM retailinvoice WHERE reg_no = ? AND DATE(accessduedate) < DATE(NOW()) AND dues > 0";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, regNo);
        return count != null && count > 0;
    }

    /** Mirrors header.jsp: loops every row and keeps the last one's nextdues value. */
    public double getNextDues(String regNo) {
        String sql = "SELECT nextdues FROM retailinvoice WHERE reg_no = ?";
        List<Double> results = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getDouble("nextdues"), regNo);
        return results.isEmpty() ? 0.0 : results.get(results.size() - 1);
    }

    public record FeesRow(double totalFees, double dues, String dueDate) {}

    /** Copied from my-profile.jsp's Fees Details tab — one row per invoice record. */
    public List<FeesRow> findFeesDetails(String regNo) {
        String sql = "SELECT round_off_total, dues, duedate FROM retailinvoice WHERE reg_no = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new FeesRow(
                rs.getDouble("round_off_total"),
                rs.getDouble("dues"),
                rs.getString("duedate")
        ), regNo);
    }
}
