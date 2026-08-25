package com.cranesvarsity.template.dao;

import com.cranesvarsity.template.dto.MarksPair;
import com.cranesvarsity.template.dto.SkillTrackerMarksRow;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Raw SQL against "manage_theory_marks" (schema cranescrm) — copied from mark-card.jsp. */
@Repository
public class ManageTheoryMarksDao {

    private final JdbcTemplate jdbcTemplate;

    public ManageTheoryMarksDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public MarksPair getMarks(String regNo, String moduleName) {
        String sql = "SELECT total_marks, obtained_marks FROM manage_theory_marks " +
                "WHERE registration_no = ? AND theory_module = ?";
        List<MarksPair> rows = jdbcTemplate.query(sql,
                (rs, rowNum) -> new MarksPair(rs.getInt("total_marks"), rs.getInt("obtained_marks")),
                regNo, moduleName);
        return rows.isEmpty() ? MarksPair.ZERO : rows.get(0);
    }

    /**
     * Every module's theory marks in ONE query, for the Report Card.
     *
     * Replaces calling {@link #getMarks} once per module. The report card ran
     * five such calls inside a loop over the student's modules — 26 queries for
     * an average student, 132 for the worst — and this backend is ~309ms from
     * its database, so that loop cost 8-41 seconds of pure round trips.
     *
     * putIfAbsent reproduces {@code rows.get(0)} exactly: the per-module query
     * takes the first row it is handed, and adding a WHERE filter removes rows
     * without reordering them, so the first row for a module is the same either
     * way. That matters — 292 student/module pairs here have duplicate rows
     * with DIFFERENT marks, so picking a different one would change what a
     * student sees.
     */
    public Map<String, MarksPair> getMarksByModule(String regNo) {
        String sql = "SELECT theory_module, total_marks, obtained_marks FROM manage_theory_marks " +
                "WHERE registration_no = ?";

        Map<String, MarksPair> byModule = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            byModule.putIfAbsent(rs.getString("theory_module"),
                    new MarksPair(rs.getInt("total_marks"), rs.getInt("obtained_marks")));
        }, regNo);
        return byModule;
    }

    /** Every module — for the Skill Tracker's Theory Results tab. Copied faithfully from student-performance.jsp. */
    public List<SkillTrackerMarksRow> findAllByRegNo(String regNo) {
        String sql = "SELECT theory_module, total_marks, obtained_marks, remarks " +
                "FROM manage_theory_marks WHERE registration_no = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new SkillTrackerMarksRow(
                rs.getString("theory_module"),
                rs.getInt("total_marks"),
                rs.getInt("obtained_marks"),
                rs.getString("remarks")
        ), regNo);
    }
}
