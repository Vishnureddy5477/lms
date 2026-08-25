package com.cranesvarsity.template.dao;

import com.cranesvarsity.template.dto.MarksPair;
import com.cranesvarsity.template.dto.SkillTrackerMarksRow;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Raw SQL against "exam_system.manage_lab_marks" — copied from mark-card.jsp. */
@Repository
public class ManageLabMarksDao {

    private final JdbcTemplate jdbcTemplate;

    public ManageLabMarksDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public MarksPair getMarks(String regNo, String moduleName) {
        String sql = "SELECT total_marks, obtained_marks FROM exam_system.manage_lab_marks " +
                "WHERE registration_no = ? AND lab_module = ?";
        List<MarksPair> rows = jdbcTemplate.query(sql,
                (rs, rowNum) -> new MarksPair(rs.getInt("total_marks"), rs.getInt("obtained_marks")),
                regNo, moduleName);
        return rows.isEmpty() ? MarksPair.ZERO : rows.get(0);
    }

    /** Every module — for the Skill Tracker's Lab Results tab. Copied faithfully from student-performance.jsp. */
    public List<SkillTrackerMarksRow> findAllByRegNo(String regNo) {
        String sql = "SELECT lab_module, total_marks, obtained_marks, remarks " +
                "FROM exam_system.manage_lab_marks WHERE registration_no = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new SkillTrackerMarksRow(
                rs.getString("lab_module"),
                rs.getInt("total_marks"),
                rs.getInt("obtained_marks"),
                rs.getString("remarks")
        ), regNo);
    }
}
