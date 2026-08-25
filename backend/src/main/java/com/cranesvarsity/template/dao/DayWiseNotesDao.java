package com.cranesvarsity.template.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Raw SQL against "daywisenotes" (schema cranescrm) — copied verbatim from class-notes.jsp. */
@Repository
public class DayWiseNotesDao {

    private final JdbcTemplate jdbcTemplate;

    public DayWiseNotesDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public record NoteRow(String postedDate, String postedBy, String batch, String module, String attachFile, String content) {}

    public List<NoteRow> findByBatch(String batch) {
        String sql = "SELECT posteddate, postedby, batch, module, attachfile, content FROM daywisenotes WHERE batch = ? ORDER BY id DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new NoteRow(
                rs.getString("posteddate"),
                rs.getString("postedby"),
                rs.getString("batch"),
                rs.getString("module"),
                rs.getString("attachfile"),
                rs.getString("content")
        ), batch);
    }
}
