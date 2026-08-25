package com.cranesvarsity.template.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Raw SQL against the legacy "trainerloginlogut" table (schema cranescrm) —
 * whether/when the trainer actually logged in for today's session, used to
 * derive the Live/Completed/Scheduled status on my-schedule.jsp.
 */
@Repository
public class TrainerLoginLogoutDao {

    private final JdbcTemplate jdbcTemplate;

    public TrainerLoginLogoutDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public record LoginWindow(String startTime, String endTime) {}

    public Optional<LoginWindow> findTodayWindow(String trainer, String modules, String batch) {
        String sql = "SELECT stime, etime FROM trainerloginlogut " +
                "WHERE trainer = ? AND attendancedate = CURDATE() AND batch LIKE ? AND modules = ?";
        List<LoginWindow> rows = jdbcTemplate.query(sql, (rs, rowNum) ->
                        new LoginWindow(rs.getString("stime"), rs.getString("etime")),
                trainer, "%" + batch + "%", modules);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }
}
