package com.cranesvarsity.template.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Raw SQL against "recordedsession" (schema cranescrm) — query copied verbatim
 * from my-recorded-sessions.jsp: soft-delete filtered, ordered by linkdate desc.
 */
@Repository
public class RecordedSessionDao {

    private final JdbcTemplate jdbcTemplate;

    public RecordedSessionDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public record Row(String batch, String subject, String recordedLink, String linkDate) {
    }

    public List<Row> findByBatch(String batch) {
        String sql = "SELECT batch, subject, recordedlink, linkdate FROM recordedsession " +
                "WHERE batch = ? AND is_deleted = ? ORDER BY linkdate DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Row(
                rs.getString("batch"),
                rs.getString("subject"),
                rs.getString("recordedlink"),
                rs.getString("linkdate")
        ), batch, "No");
    }
}
