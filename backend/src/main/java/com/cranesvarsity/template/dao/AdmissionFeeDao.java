package com.cranesvarsity.template.dao;

import com.cranesvarsity.template.dto.BillingRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Raw SQL against the legacy "AdmissionFee" table (schema cranescrm) — a
 * student can have multiple rows (one per installment/receipt), so this is
 * a JdbcTemplate DAO rather than a single-row-per-student JPA entity, same
 * reasoning as the other wide/multi-row Phase-1 DAOs. Column names copied
 * faithfully from Printreciept.jsp's SELECT * usage (my-billing-dashboard.jsp
 * only reads a few of them by positional index, which this replaces with
 * named columns).
 */
@Repository
public class AdmissionFeeDao {

    private final JdbcTemplate jdbcTemplate;

    public AdmissionFeeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<BillingRecord> findByRegNo(String regNo) {
        String sql = "SELECT reciept_no, reciept_date, stname, paid, due, conseller, enquiry_no, " +
                "course, pmode, course_fee, installmentno, admission_fee, recieved_by, batch_no " +
                "FROM AdmissionFee WHERE reg_no = ? ORDER BY reciept_no";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new BillingRecord(
                rs.getInt("reciept_no"),
                rs.getString("reciept_date"),
                rs.getString("stname"),
                rs.getDouble("paid"),
                rs.getDouble("due"),
                rs.getString("conseller"),
                rs.getString("enquiry_no"),
                rs.getString("course"),
                rs.getString("pmode"),
                rs.getDouble("course_fee"),
                rs.getInt("installmentno"),
                rs.getDouble("admission_fee"),
                rs.getString("recieved_by"),
                rs.getString("batch_no")
        ), regNo);
    }
}
