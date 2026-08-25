package com.cranesvarsity.template.dao;

import com.cranesvarsity.template.dto.MarksPair;
import com.cranesvarsity.template.dto.McqPerformanceItem;
import com.cranesvarsity.template.dto.SkillTrackerMcqRow;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Raw SQL against "exam_system.moduletestresult" — a different legacy schema
 * on the same MySQL instance/user, reached via a fully-qualified table name.
 * Query text copied faithfully from getModuleMcqData.jsp (latest attempt per
 * module, via a MAX(attempt) self-join).
 */
@Repository
public class ModuleTestResultDao {

    private final JdbcTemplate jdbcTemplate;

    public ModuleTestResultDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<McqPerformanceItem> getLatestPerModule(String regNo) {
        String sql = "SELECT r.module AS module_name, " +
                "       r.total AS total_questions, " +
                "       r.obtained AS correct_count, " +
                "       (r.total - r.obtained) AS incorrect_count, " +
                "       r.rate AS average_percent, " +
                "       r.status " +
                "FROM exam_system.moduletestresult r " +
                "JOIN ( " +
                "    SELECT module, MAX(attempt) AS latest_attempt " +
                "    FROM exam_system.moduletestresult " +
                "    WHERE reg = ? " +
                "    GROUP BY module " +
                ") t ON r.module = t.module AND r.attempt = t.latest_attempt " +
                "WHERE r.reg = ? " +
                "ORDER BY r.module";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new McqPerformanceItem(
                rs.getString("module_name"),
                rs.getInt("total_questions"),
                rs.getDouble("correct_count"),
                rs.getDouble("incorrect_count"),
                rs.getString("average_percent"),
                rs.getString("status")
        ), regNo, regNo);
    }

    /**
     * Report-card MCQ marks for one module — copied faithfully from mark-card.jsp: "total" is
     * whichever row is seen LAST while iterating (not deduped/maxed, just overwritten each loop),
     * "obtained" is the average across all matching rows (no_of_test IN (1,2)), integer division.
     */
    public MarksPair getReportCardMarks(String regNo, String moduleName) {
        String sql = "SELECT total, obtained FROM exam_system.moduletestresult " +
                "WHERE reg = ? AND module = ? AND no_of_test IN (1,2)";

        List<int[]> rows = jdbcTemplate.query(sql,
                (rs, rowNum) -> new int[]{rs.getInt("total"), rs.getInt("obtained")}, regNo, moduleName);

        if (rows.isEmpty()) {
            return MarksPair.ZERO;
        }

        int total = rows.get(rows.size() - 1)[0];
        int obtainedSum = rows.stream().mapToInt(r -> r[1]).sum();
        int obtainedAverage = obtainedSum / rows.size();

        return new MarksPair(total, obtainedAverage);
    }

    /** Every attempt, every module — for the Skill Tracker's MCQ Results tab. Copied faithfully from student-performance.jsp. */
    public List<SkillTrackerMcqRow> findAllByRegNo(String regNo) {
        String sql = "SELECT * FROM exam_system.moduletestresult WHERE reg = ? ORDER BY id";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new SkillTrackerMcqRow(
                rs.getString("module"),
                rs.getString("test"),
                rs.getString("total"),
                rs.getString("obtained"),
                rs.getString("rate"),
                rs.getString("status"),
                rs.getString("examdate"),
                rs.getString("nextdate"),
                rs.getInt("attempt")
        ), regNo);
    }

    // ─── Module Test engine (successor of validateTestAccess.jsp / manage-mcq-test-submit.jsp) ───

    /** Already passed this exact test? A pass is final — no retake is ever offered after one. */
    public boolean hasPassed(String regNo, String module, int testNo, String batch) {
        Integer n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM exam_system.moduletestresult " +
                        "WHERE reg = ? AND module = ? AND no_of_test = ? AND batchno = ? AND status = 'pass'",
                Integer.class, regNo, module, String.valueOf(testNo), batch);
        return n != null && n > 0;
    }

    /** Failed attempts so far — two of them exhausts the free allowance. */
    public int countFailedAttempts(String regNo, String module, int testNo, String batch) {
        Integer n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM exam_system.moduletestresult " +
                        "WHERE reg = ? AND module = ? AND no_of_test = ? AND batchno = ? AND status = 'fail'",
                Integer.class, regNo, module, String.valueOf(testNo), batch);
        return n == null ? 0 : n;
    }

    /** Highest attempt number recorded so far; 0 when the student has never sat this test. */
    public int maxAttempt(String regNo, String module, int testNo, String batch) {
        Integer n = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(attempt), 0) FROM exam_system.moduletestresult " +
                        "WHERE reg = ? AND module = ? AND no_of_test = ? AND batchno = ?",
                Integer.class, regNo, module, String.valueOf(testNo), batch);
        return n == null ? 0 : n;
    }

    /**
     * Save this attempt's result, keyed on the session id.
     *
     * The legacy page recomputed the attempt as MAX(attempt)+1 on every load and
     * had no unique key to upsert against, so refreshing the results page wrote
     * another row — 3,770 duplicate groups exist in production because of it,
     * one student holding 25 copies of a single attempt.
     *
     * Here the attempt number is fixed when the session opens and uq_result_session
     * makes the upsert genuinely idempotent: a resubmit updates its own row and
     * physically cannot insert a second one. Legacy rows keep session_id NULL and
     * are unaffected (MySQL permits unlimited NULLs in a unique index).
     */
    public void upsertResult(int sessionId, String stName, String regNo, String email, String module,
                             int testNo, double obtained, double total, String rate, String status,
                             int attempt, String nextDate, String batch) {
        String sql = "INSERT INTO exam_system.moduletestresult " +
                "(stname, reg, email, module, no_of_test, test, obtained, total, rate, status, " +
                " examdate, datetime, attempt, nextdate, batchno, session_id) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,NOW(),NOW(),?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE " +
                "obtained = VALUES(obtained), total = VALUES(total), rate = VALUES(rate), " +
                "status = VALUES(status), attempt = VALUES(attempt), nextdate = VALUES(nextdate), " +
                "batchno = VALUES(batchno), datetime = NOW()";

        jdbcTemplate.update(sql, stName, regNo, email, module, String.valueOf(testNo),
                "Module Test " + testNo, obtained, total, rate, status,
                attempt, nextDate, batch, sessionId);
    }
}
