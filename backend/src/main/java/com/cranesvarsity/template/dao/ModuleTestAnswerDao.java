package com.cranesvarsity.template.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Raw SQL against "exam_system.moduletestanswer" — copied from
 * getTestResults.jsp / mcq-test-result-details.jsp.
 */
@Repository
public class ModuleTestAnswerDao {

    private final JdbcTemplate jdbcTemplate;

    public ModuleTestAnswerDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public record AnswerRow(String answerId, String examId, String subject, String noOfTest, String test,
                             String question, String answer, String correctAnswer, String status,
                             String attempt, String datetime) {}

    // Both queries read the COMPATIBILITY VIEW, not the physical table.
    //
    // v_module_test_answer unions the legacy fat rows (still written by the live
    // JSP portal) with the narrow rows the new engine writes, re-expanding the
    // narrow ones by joining back to the question bank. Same column names, so
    // the report below is unchanged — but it now shows attempts taken on either
    // system. Requires mcq-module-test-reframe.sql to have been applied.

    public List<String> findDistinctSubjects(String regNo) {
        String sql = "SELECT DISTINCT subject FROM exam_system.v_module_test_answer WHERE regno = ? ORDER BY subject";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("subject"), regNo);
    }

    public List<AnswerRow> findByRegNoAndSubject(String regNo, String subject) {
        String sql = "SELECT * FROM exam_system.v_module_test_answer WHERE regno = ? AND subject = ? " +
                "ORDER BY test, attempt, datetime";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new AnswerRow(
                rs.getString("answer_id"),
                rs.getString("exam_id"),
                rs.getString("subject"),
                rs.getString("no_of_test"),
                rs.getString("test"),
                rs.getString("question"),
                rs.getString("answer"),
                rs.getString("correct_answer"),
                rs.getString("status"),
                rs.getString("attempt"),
                rs.getString("datetime")
        ), regNo, subject);
    }
}
