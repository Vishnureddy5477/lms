package com.cranesvarsity.template.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Raw SQL against "material_download_log" (schema cranescrm) — audit trail copied from materials.jsp's download branch. */
@Repository
public class MaterialDownloadLogDao {

    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JdbcTemplate jdbcTemplate;

    public MaterialDownloadLogDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(String regNo, int moduleId, String moduleName, String contentType) {
        String sql = "INSERT INTO material_download_log (reg_no, module_id, module_name, contenttype, download_date) " +
                "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, regNo, moduleId, moduleName, contentType, LocalDateTime.now().format(DATETIME_FORMAT));
    }
}
