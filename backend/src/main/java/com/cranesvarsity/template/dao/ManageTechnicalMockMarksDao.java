package com.cranesvarsity.template.dao;

import com.cranesvarsity.template.dto.SkillTrackerMockRow;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Raw SQL against "exam_system.manage_technical_mock_marks" (Hardware Mock)
 * — copied faithfully from student-performance.jsp's Hardware Mock sub-tab.
 */
@Repository
public class ManageTechnicalMockMarksDao {

    private final JdbcTemplate jdbcTemplate;

    public ManageTechnicalMockMarksDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<SkillTrackerMockRow> findByRegNo(String regNo) {
        String sql = "SELECT * FROM exam_system.manage_technical_mock_marks " +
                "WHERE reg_no = ? ORDER BY tm_mock_no ASC, mock_date DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new SkillTrackerMockRow(
                rs.getString("batch"),
                rs.getString("reg_no"),
                rs.getString("name"),
                "Technical Mock " + rs.getString("tm_mock_no"),
                rs.getString("mock_date"),
                List.of(
                        nz(rs.getString("TM_1"), "0"), nz(rs.getString("TM_2"), "0"), nz(rs.getString("TM_3"), "0"),
                        nz(rs.getString("TM_4"), "0"), nz(rs.getString("TM_5"), "0"), nz(rs.getString("TM_6"), "0"),
                        nz(rs.getString("TM_7"), "0"), nz(rs.getString("TM_8"), "0"), nz(rs.getString("TM_9"), "")
                ),
                rs.getString("avg_tm_marks") != null ? rs.getString("avg_tm_marks") : "0.0",
                rs.getString("tm_result") != null ? rs.getString("tm_result") : "N/A",
                rs.getString("created_by") != null ? rs.getString("created_by") : "System"
        ), regNo);
    }

    private static String nz(String value, String fallback) {
        return value != null ? value : fallback;
    }
}
