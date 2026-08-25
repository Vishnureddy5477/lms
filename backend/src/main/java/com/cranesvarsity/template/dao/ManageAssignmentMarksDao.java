package com.cranesvarsity.template.dao;

import com.cranesvarsity.template.dto.MarksPair;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Raw SQL against "exam_system.manage_assignment_marks" — copied from mark-card.jsp, including
 * its "only Assignment 1 counts toward the report card" convention.
 */
@Repository
public class ManageAssignmentMarksDao {

    private final JdbcTemplate jdbcTemplate;

    public ManageAssignmentMarksDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public MarksPair getMarks(String regNo, String moduleName) {
        String sql = "SELECT total_marks, obtained_marks FROM exam_system.manage_assignment_marks " +
                "WHERE registration_no = ? AND assignment_module = ? AND assignment_title = 'Assignment 1'";
        List<MarksPair> rows = jdbcTemplate.query(sql,
                (rs, rowNum) -> new MarksPair(rs.getInt("total_marks"), rs.getInt("obtained_marks")),
                regNo, moduleName);
        return rows.isEmpty() ? MarksPair.ZERO : rows.get(0);
    }
}
