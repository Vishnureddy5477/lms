package com.cranesvarsity.template.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Raw SQL against the legacy "module_status" table (schema cranescrm) — copied from current-status.jsp. */
@Repository
public class ModuleStatusDao {

    private final JdbcTemplate jdbcTemplate;

    public ModuleStatusDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<String> findStatus(String batch, String module) {
        String sql = "SELECT status FROM module_status WHERE batch = ? AND module = ?";
        List<String> rows = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("status"), batch, module);
        return rows.isEmpty() ? Optional.empty() : Optional.ofNullable(rows.get(0));
    }
}
