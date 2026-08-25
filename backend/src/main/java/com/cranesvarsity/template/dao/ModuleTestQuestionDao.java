package com.cranesvarsity.template.dao;

import com.cranesvarsity.template.dto.McqQuestionItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The question bank ("exam_system.moduletestquestions") and the per-attempt
 * snapshot of the 40 picks ("exam_system.module_test_question_set").
 *
 * Freezing the picks per attempt is carried over from the legacy portal and was
 * the right call — it is what stops a refresh from dealing a fresh set of
 * questions. What changes here is the cost and the quality of the pick:
 *
 *   Cost   — the legacy panel spent ~6 statements per session (count, select,
 *            then a 40-row batch insert). {@link #assignDistinctQuestions} does
 *            the picking AND the assigning in ONE statement.
 *
 *   Quality— two repeat problems are fixed in that same statement:
 *            1. WITHIN a test: 43 banks contain duplicate question TEXT (one
 *               bank has the same SPI question 9 times), so plain ORDER BY
 *               RAND() could serve a student the same question several times.
 *               Collapsing on question text kills that.
 *            2. ACROSS attempts: only 80 of 152 banks hold the 80+ questions
 *               needed for two non-overlapping sets, so a retake used to repeat
 *               heavily. Questions the student has already been served are
 *               sorted last, so a retake exhausts the unseen pool first.
 */
@Repository
public class ModuleTestQuestionDao {

    private final JdbcTemplate jdbcTemplate;

    public ModuleTestQuestionDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Raw active rows — the number the legacy >= 40 gate counted. */
    public int countActive(String module, int testNo) {
        Integer n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM exam_system.moduletestquestions " +
                        "WHERE modules = ? AND mcq_test_no = ? AND active_deactive = 'Active'",
                Integer.class, module, testNo);
        return n == null ? 0 : n;
    }

    /** Genuinely distinct questions — what the student can be served without repeats. */
    public int countDistinctActive(String module, int testNo) {
        Integer n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM (" +
                        "  SELECT 1 FROM exam_system.moduletestquestions " +
                        "  WHERE modules = ? AND mcq_test_no = ? AND active_deactive = 'Active' " +
                        "  GROUP BY questions) t",
                Integer.class, module, testNo);
        return n == null ? 0 : n;
    }

    public int countAssigned(int sessionId) {
        Integer n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM exam_system.module_test_question_set WHERE session_id = ?",
                Integer.class, sessionId);
        return n == null ? 0 : n;
    }

    /**
     * Pick and assign up to {@code limit} DISTINCT questions in a single
     * statement, preferring ones this student has not been served before.
     *
     * The history side deliberately unions the new snapshot table with the
     * legacy moduletestquestions_assigned, so attempts taken on the old JSP
     * portal still count as "seen" and a retake here does not re-serve them.
     *
     * @return how many slots were filled (may be < limit on a thin bank)
     */
    public int assignDistinctQuestions(int sessionId, String regNo, String module, int testNo, int limit) {
        String sql =
                "INSERT INTO exam_system.module_test_question_set (session_id, question_id, question_order) " +
                "SELECT ?, t.id, ROW_NUMBER() OVER (ORDER BY t.seen, t.r) " +
                "FROM ( " +
                "  SELECT d.id AS id, (h.question_id IS NOT NULL) AS seen, RAND() AS r " +
                "  FROM ( " +
                // one row per distinct question text — kills in-test duplicates
                "    SELECT MIN(q.id) AS id " +
                "    FROM exam_system.moduletestquestions q " +
                "    WHERE q.modules = ? AND q.mcq_test_no = ? AND q.active_deactive = 'Active' " +
                "    GROUP BY q.questions " +
                "  ) d " +
                "  LEFT JOIN ( " +
                // everything this student has already been served for this module+test
                "    SELECT s2.question_id AS question_id " +
                "    FROM exam_system.module_test_question_set s2 " +
                "    JOIN cranescrm.moduletestlogin l1 ON l1.id = s2.session_id " +
                "    WHERE l1.regno = ? AND l1.module = ? AND l1.no_of_test = ? " +
                "    UNION " +
                "    SELECT a2.question_id AS question_id " +
                "    FROM exam_system.moduletestquestions_assigned a2 " +
                "    JOIN cranescrm.moduletestlogin l2 ON l2.id = a2.exam_id " +
                "    WHERE l2.regno = ? AND l2.module = ? AND l2.no_of_test = ? " +
                "  ) h ON h.question_id = d.id " +
                "  ORDER BY seen ASC, r ASC " +
                "  LIMIT ? " +
                ") t";

        return jdbcTemplate.update(sql, sessionId, module, testNo,
                regNo, module, testNo, regNo, module, testNo, limit);
    }

    /**
     * Top the snapshot up to the full slot count when the bank cannot supply
     * enough distinct questions.
     *
     * Only three banks in production need this today (CPP-VLSI test 1 has 99
     * rows but 30 distinct; On Chip Protocols Verification test 2 has 50/15;
     * QT Application test 1 has 40/20). Backfilling keeps those tests running
     * rather than blocking a scheduled exam — the service logs a warning so the
     * bank gets authored properly.
     */
    public int backfillDuplicates(int sessionId, String module, int testNo, int startOrder, int need) {
        String sql =
                "INSERT INTO exam_system.module_test_question_set (session_id, question_id, question_order) " +
                "SELECT ?, t.id, ? + ROW_NUMBER() OVER (ORDER BY t.r) " +
                "FROM ( " +
                "  SELECT q.id AS id, RAND() AS r " +
                "  FROM exam_system.moduletestquestions q " +
                "  WHERE q.modules = ? AND q.mcq_test_no = ? AND q.active_deactive = 'Active' " +
                "  ORDER BY r LIMIT ? " +
                ") t";
        return jdbcTemplate.update(sql, sessionId, startOrder, module, testNo, need);
    }

    /** The frozen question list for this attempt, in slot order. Never includes the answer key. */
    public List<McqQuestionItem> findAssigned(int sessionId) {
        String sql = "SELECT s.question_order, s.question_id, q.questions, q.opt1, q.opt2, q.opt3, q.opt4 " +
                "FROM exam_system.module_test_question_set s " +
                "JOIN exam_system.moduletestquestions q ON q.id = s.question_id " +
                "WHERE s.session_id = ? ORDER BY s.question_order";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new McqQuestionItem(
                rs.getInt("question_order"),
                rs.getInt("question_id"),
                rs.getString("questions"),
                rs.getString("opt1"),
                rs.getString("opt2"),
                rs.getString("opt3"),
                rs.getString("opt4")
        ), sessionId);
    }

    /**
     * The answer key for this attempt, as slot -> correct option.
     *
     * Server-side only. Fetched once per save batch so correctness is decided
     * here, never by the client, and the key is never serialised to the browser.
     */
    public Map<Integer, Integer> findAnswerKey(int sessionId) {
        String sql = "SELECT s.question_order, q.correct_option " +
                "FROM exam_system.module_test_question_set s " +
                "JOIN exam_system.moduletestquestions q ON q.id = s.question_id " +
                "WHERE s.session_id = ?";

        Map<Integer, Integer> key = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            key.put(rs.getInt("question_order"), rs.getInt("correct_option"));
        }, sessionId);
        return key;
    }

    /** Slot -> question id, so a saved answer records which question filled that slot. */
    public Map<Integer, Integer> findSlotQuestionIds(int sessionId) {
        Map<Integer, Integer> slots = new HashMap<>();
        jdbcTemplate.query(
                "SELECT question_order, question_id FROM exam_system.module_test_question_set WHERE session_id = ?",
                rs -> { slots.put(rs.getInt("question_order"), rs.getInt("question_id")); },
                sessionId);
        return slots;
    }
}
