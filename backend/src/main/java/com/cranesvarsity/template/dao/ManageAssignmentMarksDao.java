package com.cranesvarsity.template.dao;

import com.cranesvarsity.template.dto.MarksPair;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Every module's assignment marks in ONE query, for the Report Card.
     *
     * Replaces calling {@link #getMarks} once per module inside a loop. See
     * ManageTheoryMarksDao#getMarksByModule for why putIfAbsent reproduces the
     * per-module {@code rows.get(0)} behaviour exactly.
     */
    public Map<String, MarksPair> getMarksByModule(String regNo) {
        String sql = "SELECT assignment_module, total_marks, obtained_marks FROM exam_system.manage_assignment_marks " +
                "WHERE registration_no = ? AND assignment_title = 'Assignment 1'";

        Map<String, MarksPair> byModule = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            byModule.putIfAbsent(rs.getString("assignment_module"),
                    new MarksPair(rs.getInt("total_marks"), rs.getInt("obtained_marks")));
        }, regNo);
        return byModule;
    }
}
