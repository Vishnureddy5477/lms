package com.cranesvarsity.template.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Raw SQL against the legacy "studentdetails" table (schema cranescrm).
 * Gates whether the placement/JD stat cards show real numbers, mirroring
 * the "remarks" check in index.jsp.
 */
@Repository
public class StudentDetailsDao {

    private final JdbcTemplate jdbcTemplate;

    public StudentDetailsDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<String> getRemarks(String regNo) {
        String sql = "SELECT remarks FROM studentdetails WHERE regno = ?";
        List<String> results = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("remarks"), regNo);
        return results.isEmpty() ? Optional.empty() : Optional.ofNullable(results.get(results.size() - 1));
    }
}
