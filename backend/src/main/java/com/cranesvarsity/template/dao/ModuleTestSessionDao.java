package com.cranesvarsity.template.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/**
 * The active test session — "cranescrm.moduletestlogin". Its {@code id} is the
 * attempt identity every other table hangs off (legacy code calls it exam_id).
 *
 * Successor of the session handling in validateTestAccess.jsp and
 * manage-mcq-test-panel.jsp, with two corrections carried over from the
 * reframe:
 *   - attempt_no is fixed at session START (the legacy code recomputed
 *     MAX(attempt)+1 at submit time, which is why a refresh of the results page
 *     could write a second result row — 3,770 such groups exist in production).
 *   - violation_count / auto_submitted live here so proctoring state survives a
 *     refresh and is never client-owned.
 */
@Repository
public class ModuleTestSessionDao {

    private final JdbcTemplate jdbcTemplate;

    public ModuleTestSessionDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public record SessionRow(
            int id, String regNo, String module, int noOfTest, String test,
            String loginDate, String loginStatus, Integer attemptNo,
            int violationCount, boolean autoSubmitted
    ) {}

    /**
     * Release this student's OTHER open sessions so a crashed or abandoned tab
     * can never permanently lock them out of starting a different test.
     *
     * The NOT(...) clause is the important half: the session for the test they
     * are actually resuming is deliberately left open, so it resumes exactly
     * where they left off. Net rule — one active test session per student.
     */
    public int releaseOtherSessions(String regNo, String module, int noOfTest) {
        String sql = "UPDATE cranescrm.moduletestlogin SET loginstatus = 'no' " +
                "WHERE regno = ? AND loginstatus = 'yes' " +
                "AND NOT (module = ? AND no_of_test = ?)";
        return jdbcTemplate.update(sql, regNo, module, noOfTest);
    }

    /** The still-open session for THIS module + test, if the student is resuming one. */
    public Optional<SessionRow> findOpenSession(String regNo, String module, int noOfTest) {
        String sql = "SELECT id, regno, module, no_of_test, test, logindate, loginstatus, " +
                "attempt_no, violation_count, auto_submitted " +
                "FROM cranescrm.moduletestlogin " +
                "WHERE regno = ? AND module = ? AND no_of_test = ? AND loginstatus = 'yes' " +
                "ORDER BY id DESC LIMIT 1";
        List<SessionRow> rows = jdbcTemplate.query(sql, this::mapSession, regNo, module, noOfTest);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public Optional<SessionRow> findById(int sessionId) {
        String sql = "SELECT id, regno, module, no_of_test, test, logindate, loginstatus, " +
                "attempt_no, violation_count, auto_submitted " +
                "FROM cranescrm.moduletestlogin WHERE id = ?";
        List<SessionRow> rows = jdbcTemplate.query(sql, this::mapSession, sessionId);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    /**
     * Open a new session and return its generated id.
     *
     * mcq_test_id and no_of_test are both set to the test number, matching the
     * legacy insert — that keeps a session opened here resumable by the old JSP
     * portal, which looks sessions up by mcq_test_id.
     */
    public int createSession(String regNo, String module, String test, String testDate,
                             String loginDateTime, String loginTime, int noOfTest, int attemptNo) {
        String sql = "INSERT INTO cranescrm.moduletestlogin " +
                "(regno, module, test, testdate, logindate, logintime, loginstatus, " +
                " mcq_test_id, no_of_test, attempt_no, violation_count, auto_submitted) " +
                "VALUES (?,?,?,?,?,?,'yes',?,?,?,0,0)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, regNo);
            ps.setString(2, module);
            ps.setString(3, test);
            ps.setString(4, testDate);
            ps.setString(5, loginDateTime);
            ps.setString(6, loginTime);
            ps.setInt(7, noOfTest);
            ps.setInt(8, noOfTest);
            ps.setInt(9, attemptNo);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to open a test session for regno=" + regNo);
        }
        return key.intValue();
    }

    /** Record a strike and return the SERVER's new running total. */
    public int incrementViolation(int sessionId) {
        jdbcTemplate.update(
                "UPDATE cranescrm.moduletestlogin SET violation_count = violation_count + 1 WHERE id = ?",
                sessionId);
        Integer count = jdbcTemplate.queryForObject(
                "SELECT violation_count FROM cranescrm.moduletestlogin WHERE id = ?", Integer.class, sessionId);
        return count == null ? 0 : count;
    }

    public void logViolation(int sessionId, String type, String reason) {
        jdbcTemplate.update(
                "INSERT INTO cranescrm.module_test_violation (session_id, type, reason) VALUES (?,?,?)",
                sessionId, type, reason);
    }

    /** Close the session out at submission. */
    public void closeSession(int sessionId, boolean autoSubmitted) {
        jdbcTemplate.update(
                "UPDATE cranescrm.moduletestlogin " +
                        "SET loginstatus = 'no', attempt_status = 'SUBMITTED', auto_submitted = ? " +
                        "WHERE id = ?",
                autoSubmitted ? 1 : 0, sessionId);
    }

    private SessionRow mapSession(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        // wasNull() reflects the LAST getter called, so capture it immediately.
        int attempt = rs.getInt("attempt_no");
        Integer attemptNo = rs.wasNull() ? null : attempt;
        return new SessionRow(
                rs.getInt("id"),
                rs.getString("regno"),
                rs.getString("module"),
                rs.getInt("no_of_test"),
                rs.getString("test"),
                rs.getString("logindate"),
                rs.getString("loginstatus"),
                attemptNo,
                rs.getInt("violation_count"),
                rs.getBoolean("auto_submitted")
        );
    }
}
