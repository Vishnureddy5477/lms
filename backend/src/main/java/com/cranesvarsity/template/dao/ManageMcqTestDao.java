package com.cranesvarsity.template.dao;

import com.cranesvarsity.template.dto.AvailableTestItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Raw SQL against "exam_system.manage_mcq_test" / "exam_system.moduletestresult" —
 * query copied verbatim from getAvailableTests.jsp: a UNION ALL of not-yet-attempted
 * scheduled tests and eligible retakes (latest failed attempt, cooldown date reached,
 * never passed).
 */
@Repository
public class ManageMcqTestDao {

    private final JdbcTemplate jdbcTemplate;

    public ManageMcqTestDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AvailableTestItem> findAvailableTests(String regNo, String batchNo, String currentDate) {
        String sql =
                "SELECT " +
                "    'scheduled' as test_type, " +
                "    mmt.id, " +
                "    mmt.module_name, " +
                "    mmt.batch_name, " +
                "    mmt.mcq_test_no, " +
                "    mmt.test_date, " +
                "    mmt.test_start_time, " +
                "    mmt.test_end_time, " +
                "    0 as attempt_number " +
                "FROM exam_system.manage_mcq_test mmt " +
                "WHERE mmt.batch_name = ? " +
                "    AND mmt.test_date >= ? " +
                "    AND NOT EXISTS (" +
                "        SELECT 1 FROM exam_system.moduletestresult mtr " +
                "        WHERE mtr.reg = ? " +
                "        AND mtr.module = mmt.module_name " +
                "        AND mtr.no_of_test = mmt.mcq_test_no " +
                "        AND mtr.batchno = mmt.batch_name" +
                "    ) " +

                "UNION ALL " +

                "SELECT " +
                "    'retake' as test_type, " +
                "    CONCAT('retake_', REPLACE(mr.module, ' ', '_'), '_', mr.no_of_test) as id, " +
                "    mr.module as module_name, " +
                "    mr.batchno as batch_name, " +
                "    mr.no_of_test as mcq_test_no, " +
                "    mr.nextdate as test_date, " +
                "    COALESCE(mmt.test_start_time, '09:00:00') as test_start_time, " +
                "    COALESCE(mmt.test_end_time, '18:00:00') as test_end_time, " +
                "    mr.attempt as attempt_number " +
                "FROM exam_system.moduletestresult mr " +
                "LEFT JOIN exam_system.manage_mcq_test mmt ON mmt.id = ( " +
                "    SELECT x.id FROM exam_system.manage_mcq_test x " +
                "    WHERE x.module_name = mr.module AND x.mcq_test_no = mr.no_of_test " +
                "      AND x.batch_name = mr.batchno " +
                "    ORDER BY x.test_date DESC, x.id DESC LIMIT 1 ) " +
                "WHERE mr.reg = ? " +
                "    AND mr.nextdate IS NOT NULL " +
                "    AND mr.nextdate != '' " +
                "    AND mr.nextdate != 'NA' " +
                "    AND mr.nextdate >= ? " +
                "    AND mr.status = 'fail' " +
                "    AND mr.attempt = (" +
                "        SELECT MAX(mr2.attempt) " +
                "        FROM exam_system.moduletestresult mr2 " +
                "        WHERE mr2.reg = mr.reg " +
                "        AND mr2.module = mr.module " +
                "        AND mr2.no_of_test = mr.no_of_test " +
                "        AND mr2.batchno = mr.batchno " +
                "        AND mr2.status = 'fail'" +
                "    ) " +
                "    AND NOT EXISTS (" +
                "        SELECT 1 FROM exam_system.moduletestresult mr3 " +
                "        WHERE mr3.reg = mr.reg " +
                "        AND mr3.module = mr.module " +
                "        AND mr3.no_of_test = mr.no_of_test " +
                "        AND mr3.batchno = mr.batchno " +
                "        AND mr3.status = 'pass'" +
                "    ) " +

                "ORDER BY test_date ASC, test_start_time ASC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new AvailableTestItem(
                rs.getString("id"),
                rs.getString("module_name"),
                rs.getString("batch_name"),
                rs.getString("mcq_test_no"),
                rs.getString("test_date"),
                rs.getString("test_start_time"),
                rs.getString("test_end_time"),
                rs.getString("test_type")
        ), batchNo, currentDate, regNo, regNo, currentDate);
    }
}
