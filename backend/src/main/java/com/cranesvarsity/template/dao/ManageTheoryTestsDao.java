package com.cranesvarsity.template.dao;

import com.cranesvarsity.template.dto.TheoryTestItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Raw SQL against "exam_system.manage_theory_tests" — copied from
 * TheoryTestScheduleServlet's getTestsForBatch action. Only the schedule
 * listing is ported here (not assignAndGetQuestion / PDF viewing, which
 * is out of scope for this pass).
 */
@Repository
public class ManageTheoryTestsDao {

    private final JdbcTemplate jdbcTemplate;

    public ManageTheoryTestsDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<TheoryTestItem> findByBatch(String batchNo) {
        String sql = "SELECT theory_test_id, domain_name, test_type, module_name, batch_name, " +
                "       DATE_FORMAT(test_date, '%Y-%m-%d') AS test_date, " +
                "       test_start_time, test_end_time, total_marks, total_questions " +
                "FROM exam_system.manage_theory_tests " +
                "WHERE batch_name = ? " +
                "  AND test_date >= CURDATE() - INTERVAL 1 DAY " +
                "ORDER BY test_date ASC, test_start_time ASC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            String testType = rs.getString("test_type");
            return new TheoryTestItem(
                    rs.getString("theory_test_id"),
                    rs.getString("domain_name"),
                    (testType == null || testType.isBlank()) ? "theory" : testType.toLowerCase(),
                    rs.getString("module_name"),
                    rs.getString("batch_name"),
                    rs.getString("test_date"),
                    rs.getString("test_start_time"),
                    rs.getString("test_end_time"),
                    rs.getString("total_marks"),
                    rs.getString("total_questions")
            );
        }, batchNo);
    }
}
