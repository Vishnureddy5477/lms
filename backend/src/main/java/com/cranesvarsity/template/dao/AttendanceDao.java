package com.cranesvarsity.template.dao;

import com.cranesvarsity.template.dto.AttendanceDetailRecord;
import com.cranesvarsity.template.dto.AttendanceSummaryItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

/**
 * Raw SQL against the legacy "attendance" table (schema cranescrm).
 * Query text copied faithfully from getModuleAttendanceData.jsp.
 */
@Repository
public class AttendanceDao {

    private final JdbcTemplate jdbcTemplate;

    public AttendanceDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AttendanceSummaryItem> getModuleSummary(String regNo) {
        String sql = "SELECT module, " +
                "COUNT(*) AS total_classes, " +
                "SUM(CASE WHEN present_absent IN ('Present','P') THEN 1 ELSE 0 END) AS present_count, " +
                "SUM(CASE WHEN present_absent IN ('Absent','A') THEN 1 ELSE 0 END) AS absent_count " +
                "FROM attendance WHERE reg_no = ? GROUP BY module ORDER BY module";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            int total = rs.getInt("total_classes");
            int present = rs.getInt("present_count");
            int absent = rs.getInt("absent_count");
            double average = total > 0 ? (present * 100.0 / total) : 0.0;
            return new AttendanceSummaryItem(rs.getString("module"), total, present, absent, average);
        }, regNo);
    }

    /** Used as the "Course Start Date" shown on the dashboard, same as header.jsp's bStartDate. */
    public LocalDate getEarliestClassDate(String regNo) {
        String sql = "SELECT DATE(classdate) AS classdate FROM attendance WHERE reg_no = ? ORDER BY classdate ASC LIMIT 1";
        List<LocalDate> dates = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Date d = rs.getDate("classdate");
            return d != null ? d.toLocalDate() : null;
        }, regNo);
        return dates.isEmpty() ? null : dates.get(0);
    }

    /** Distinct module names for a whole batch — used by current-status.jsp (Course Progress). */
    public List<String> findDistinctModulesByBatch(String batch) {
        String sql = "SELECT DISTINCT module FROM attendance WHERE batch = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("module"), batch);
    }

    public record ModuleDateRange(String startDate, String endDate, int totalDays) {}

    /** Copied from current-status.jsp: MIN/MAX(classdate) + COUNT(DISTINCT classdate) for one module. */
    public ModuleDateRange getModuleDateRange(String batch, String module) {
        String sql = "SELECT MIN(DATE(classdate)) AS start_date, MAX(DATE(classdate)) AS end_date, " +
                "COUNT(DISTINCT classdate) AS total_days FROM attendance WHERE batch = ? AND module = ?";
        List<ModuleDateRange> rows = jdbcTemplate.query(sql, (rs, rowNum) -> new ModuleDateRange(
                rs.getString("start_date"), rs.getString("end_date"), rs.getInt("total_days")
        ), batch, module);
        return rows.isEmpty() ? new ModuleDateRange("NA", "NA", 0) : rows.get(0);
    }

    /**
     * Distinct module names a student has attendance for — legacy mark-card.jsp uses this same
     * query as the module list backing the report card. Ordered alphabetically for a deterministic
     * result (legacy relies on implicit DB ordering, which MySQL doesn't actually guarantee).
     */
    public List<String> findDistinctModules(String regNo) {
        String sql = "SELECT DISTINCT module FROM attendance WHERE reg_no = ? ORDER BY module";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("module"), regNo);
    }

    /** Full per-class record list — backs the Attendance Details page (my-attendance.jsp successor). */
    public List<AttendanceDetailRecord> findDetailsByRegNo(String regNo) {
        String sql = "SELECT module, classdate, present_absent, starttime, endtime, trainer " +
                "FROM attendance WHERE reg_no = ? ORDER BY module, classdate DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Date classDate = rs.getDate("classdate");
            return new AttendanceDetailRecord(
                    rs.getString("module"),
                    classDate != null ? classDate.toLocalDate().toString() : null,
                    rs.getString("present_absent"),
                    rs.getString("starttime"),
                    rs.getString("endtime"),
                    rs.getString("trainer")
            );
        }, regNo);
    }
}
