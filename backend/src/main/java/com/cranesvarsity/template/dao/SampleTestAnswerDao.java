package com.cranesvarsity.template.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Raw SQL against "exam_system.manage_sampletest_answer" — copied from mcq-practice-test-results.jsp. */
@Repository
public class SampleTestAnswerDao {

    private final JdbcTemplate jdbcTemplate;

    public SampleTestAnswerDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public record ResultRow(
            String moduleName, String chapter, int totalCorrect, int totalIncorrect,
            int totalNotAttempt, double marksPercent, String examDate
    ) {}

    public List<ResultRow> findByRegNo(String regNo) {
        String sql = "SELECT modules, chapters, total_correct, total_incorrect, total_notattempt, marks_per, examdate " +
                "FROM exam_system.manage_sampletest_answer WHERE regno = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ResultRow(
                rs.getString("modules"),
                rs.getString("chapters"),
                rs.getInt("total_correct"),
                rs.getInt("total_incorrect"),
                rs.getInt("total_notattempt"),
                rs.getDouble("marks_per"),
                rs.getString("examdate")
        ), regNo);
    }
}
