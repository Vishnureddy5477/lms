package com.cranesvarsity.template.dao;

import com.cranesvarsity.template.dto.TheoryQuestionPaperFile;
import com.cranesvarsity.template.dto.TheoryTestScheduleRow;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Raw SQL against the three legacy exam_system tables behind the Theory/Lab
 * Test page — ported from TheoryTestScheduleServlet:
 *
 *   manage_theory_tests            the schedule; its "questions" column is a
 *                                  comma-separated pool of question-paper ids
 *   manage_theory_questions_pdf    id -> theory_pdf_link (one PDF variant)
 *   theory_test_assigned_questions which variant a given student was locked to
 *
 * Theory and lab tests are the same rows; test_type only tags which is which.
 */
@Repository
public class ManageTheoryTestsDao {

    private final JdbcTemplate jdbcTemplate;

    public ManageTheoryTestsDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** The schedule listing: everything for this batch from yesterday onwards. */
    public List<TheoryTestScheduleRow> findByBatch(String batchNo) {
        String sql = "SELECT theory_test_id, domain_name, test_type, module_name, batch_name, " +
                "       DATE_FORMAT(test_date, '%Y-%m-%d') AS test_date, " +
                "       test_start_time, test_end_time, total_marks, total_questions " +
                "FROM exam_system.manage_theory_tests " +
                "WHERE batch_name = ? " +
                "  AND test_date >= CURDATE() - INTERVAL 1 DAY " +
                "ORDER BY test_date ASC, test_start_time ASC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new TheoryTestScheduleRow(
                rs.getString("theory_test_id"),
                rs.getString("domain_name"),
                rs.getString("test_type"),
                rs.getString("module_name"),
                rs.getString("batch_name"),
                rs.getString("test_date"),
                rs.getString("test_start_time"),
                rs.getString("test_end_time"),
                rs.getString("total_marks"),
                rs.getString("total_questions"),
                null
        ), batchNo);
    }

    /**
     * One schedule row, scoped to the student's OWN batch.
     *
     * The batch predicate is the ownership check: a student cannot name another
     * batch's theory_test_id and have it resolve.
     */
    public Optional<TheoryTestScheduleRow> findForBatch(String theoryTestId, String batchNo) {
        String sql = "SELECT theory_test_id, test_type, module_name, batch_name, " +
                "       DATE_FORMAT(test_date, '%Y-%m-%d') AS test_date, " +
                "       test_start_time, test_end_time, questions " +
                "FROM exam_system.manage_theory_tests " +
                "WHERE theory_test_id = ? AND batch_name = ? " +
                "LIMIT 1";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new TheoryTestScheduleRow(
                rs.getString("theory_test_id"),
                null,
                rs.getString("test_type"),
                rs.getString("module_name"),
                rs.getString("batch_name"),
                rs.getString("test_date"),
                rs.getString("test_start_time"),
                rs.getString("test_end_time"),
                null,
                null,
                rs.getString("questions")
        ), theoryTestId, batchNo).stream().findFirst();
    }

    /** The variant this student was already locked to, if any. */
    public Optional<String> findAssignedQuestionId(String theoryTestId, String regNo) {
        String sql = "SELECT MIN(question_id) AS question_id " +
                "FROM exam_system.theory_test_assigned_questions " +
                "WHERE theory_test_id = ? AND reg_no = ?";
        List<String> found = jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getString("question_id"), theoryTestId, regNo);
        return found.stream().filter(q -> q != null && !q.isBlank()).findFirst();
    }

    /**
     * Lock this student to a variant, but only if nothing is locked yet.
     *
     * The WHERE NOT EXISTS makes a double-click (or two tabs) a no-op rather
     * than a second row, so the caller can safely re-read the assignment
     * afterwards and get the same answer either way.
     */
    public void assignQuestionIfAbsent(String theoryTestId, String regNo, String questionId) {
        String sql = "INSERT INTO exam_system.theory_test_assigned_questions " +
                "       (theory_test_id, reg_no, question_id) " +
                "SELECT ?, ?, ? FROM DUAL WHERE NOT EXISTS ( " +
                "  SELECT 1 FROM exam_system.theory_test_assigned_questions " +
                "  WHERE theory_test_id = ? AND reg_no = ?)";
        jdbcTemplate.update(sql, theoryTestId, regNo, questionId, theoryTestId, regNo);
    }

    /**
     * One question-paper variant: its storage URL and its trainer-facing label.
     *
     * Deliberately does NOT filter on is_deleted. A paper already handed to a
     * student is theirs for the duration; a trainer soft-deleting it mid-test
     * should not blank the screen of someone halfway through answering it.
     */
    public Optional<TheoryQuestionPaperFile> findQuestionPaperFile(String questionId) {
        String sql = "SELECT theory_pdf_link, pdf_code FROM exam_system.manage_theory_questions_pdf " +
                "WHERE id = ? LIMIT 1";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new TheoryQuestionPaperFile(
                        rs.getString("theory_pdf_link"),
                        rs.getString("pdf_code")), questionId)
                .stream()
                .filter(file -> file.link() != null && !file.link().isBlank())
                .findFirst();
    }
}
