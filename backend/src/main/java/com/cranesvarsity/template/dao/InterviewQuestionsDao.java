package com.cranesvarsity.template.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Raw SQL against "manage_interview_questions" / "manage_interview_questions_answer"
 * (schema cranescrm) — copied from manage-interview-questions.jsp /
 * view-interview-questions.jsp / get-interview-answers.jsp. Not student-scoped —
 * same global content for every student, matching legacy exactly.
 */
@Repository
public class InterviewQuestionsDao {

    private final JdbcTemplate jdbcTemplate;

    public InterviewQuestionsDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public record CompanyModuleRow(String clientName, String moduleCategory, String moduleName, int questionCount) {}
    public record QuestionRow(int id, String moduleCategory, String moduleName, String question, int answerCount) {}
    public record AnswerRow(int answerId, String answerText, int ansCount, String createdBy, String createdAt) {}

    public List<CompanyModuleRow> findCompanyModuleSummary() {
        String sql = "SELECT client_name, module_category, module_name, COUNT(*) AS question_count " +
                "FROM manage_interview_questions " +
                "WHERE status = 0 AND isdelete = 0 " +
                "GROUP BY client_name, module_category, module_name " +
                "ORDER BY client_name, module_category, module_name";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new CompanyModuleRow(
                rs.getString("client_name"), rs.getString("module_category"),
                rs.getString("module_name"), rs.getInt("question_count")
        ));
    }

    public List<QuestionRow> findQuestionsByModuleCategory(String moduleCategory) {
        String sql = "SELECT q.id, q.module_category, q.module_name, q.question, COUNT(a.answer_id) AS answer_count " +
                "FROM manage_interview_questions q " +
                "LEFT JOIN manage_interview_questions_answer a ON a.qst_id = q.id " +
                "WHERE q.module_category = ? " +
                "GROUP BY q.id, q.module_category, q.module_name, q.question " +
                "ORDER BY q.id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new QuestionRow(
                rs.getInt("id"), rs.getString("module_category"), rs.getString("module_name"),
                rs.getString("question"), rs.getInt("answer_count")
        ), moduleCategory);
    }

    public List<AnswerRow> findAnswersByQuestionId(int qstId) {
        String sql = "SELECT answer_id, answer_text, ans_count, created_by, " +
                "DATE_FORMAT(created_at, '%d-%m-%Y %H:%i') AS created_at " +
                "FROM manage_interview_questions_answer WHERE qst_id = ? ORDER BY ans_count ASC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new AnswerRow(
                rs.getInt("answer_id"), rs.getString("answer_text"), rs.getInt("ans_count"),
                rs.getString("created_by"), rs.getString("created_at")
        ), qstId);
    }
}
