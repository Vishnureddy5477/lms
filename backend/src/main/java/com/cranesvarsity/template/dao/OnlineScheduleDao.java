package com.cranesvarsity.template.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Raw SQL against the legacy "onlineschedule" table (schema cranescrm).
 * Query text copied faithfully from my-schedule.jsp (batch match is a LIKE
 * '%batch%' in legacy since one onlineschedule row can cover a combined
 * batch group, not just this student's exact batchno).
 */
@Repository
public class OnlineScheduleDao {

    private final JdbcTemplate jdbcTemplate;

    public OnlineScheduleDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public record TodayRow(String module, String startTime, String endTime, String trainer, String scheduleLink) {}

    public record WeekRow(String module, String startTime, String endTime, String startDate, String endDate, String mode, String trainer) {}

    public List<TodayRow> findToday(String batch) {
        String sql = "SELECT modules, stime, etime, trainer, schedulelink FROM onlineschedule " +
                "WHERE batchno LIKE ? AND CURDATE() BETWEEN sdate AND edate";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new TodayRow(
                rs.getString("modules"),
                rs.getString("stime"),
                rs.getString("etime"),
                rs.getString("trainer"),
                rs.getString("schedulelink")
        ), "%" + batch + "%");
    }

    public List<WeekRow> findWeek(String batch) {
        String sql = "SELECT modules, stime, etime, sdate, edate, mode, trainer FROM onlineschedule " +
                "WHERE batchno LIKE ? AND sdate <= CURDATE() + INTERVAL 7 DAY AND edate >= CURDATE() " +
                "ORDER BY sdate, stime";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new WeekRow(
                rs.getString("modules"),
                rs.getString("stime"),
                rs.getString("etime"),
                rs.getString("sdate"),
                rs.getString("edate"),
                rs.getString("mode"),
                rs.getString("trainer")
        ), "%" + batch + "%");
    }
}
