package com.cranesvarsity.template.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Raw SQL against "programming_practice_questions" (schema cranescrm) — copied from programming-practice-questions.jsp. */
@Repository
public class ProgrammingPracticeQuestionsDao {

    private final JdbcTemplate jdbcTemplate;

    public ProgrammingPracticeQuestionsDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public record LinkRow(String title, String link) {}

    public List<LinkRow> findAll() {
        String sql = "SELECT title, link FROM programming_practice_questions";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new LinkRow(rs.getString("title"), rs.getString("link")));
    }
}
