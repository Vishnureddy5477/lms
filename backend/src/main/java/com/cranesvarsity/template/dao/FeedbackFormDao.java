package com.cranesvarsity.template.dao;

import com.cranesvarsity.template.dto.FeedbackFormData;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Raw SQL against "feedbackformdetail" / "access_module_feedback" /
 * "module_status" (schema cranescrm) — copied from feedback-form-list.jsp,
 * StudentFeedBackFormModuleWise.jsp, EditStudentFeedBack.jsp and
 * feedback-form-details.jsp.
 */
@Repository
public class FeedbackFormDao {

    private final JdbcTemplate jdbcTemplate;

    public FeedbackFormDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean hasActiveModuleFeedbackAccess(String regNo) {
        String sql = "SELECT COUNT(*) FROM access_module_feedback WHERE reg_no = ? AND is_active = 'Yes'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, regNo);
        return count != null && count > 0;
    }

    public record ModuleStatusRow(String module, String trainer, String entryDate) {}

    /**
     * When access is "active", every completed module shows (regardless of whether feedback was
     * already given — feedback-form-list.jsp's own logic, not narrowed down here); otherwise only
     * modules completed since 2024-07-24 with feedback_given='No'. Both branches copied verbatim,
     * plus a "batch IS NOT NULL AND batch <> ''" guard on the module_status side that legacy did
     * NOT have — a blank batch there must never match a caller's blank batch as if it were a real
     * pairing (the caller is also expected to never pass a blank batch, see FeedbackService).
     */
    public List<ModuleStatusRow> findPendingModules(String batch, boolean activeAccess) {
        String sql = activeAccess
                ? "SELECT module, trainer, DATE(entrydate) as entrydate FROM module_status WHERE batch = ? AND STATUS = 'Completed' AND batch IS NOT NULL AND batch <> ''"
                : "SELECT module, trainer, DATE(entrydate) as entrydate FROM module_status WHERE batch = ? AND STATUS = 'Completed' AND entrydate >= '2024-07-24' AND feedback_given = 'No' AND batch IS NOT NULL AND batch <> ''";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new ModuleStatusRow(
                rs.getString("module"), rs.getString("trainer"), rs.getString("entrydate")
        ), batch);
    }

    public boolean existsFeedback(String regNo, String batch, String module, String trainer) {
        String sql = "SELECT COUNT(*) FROM feedbackformdetail WHERE regno = ? AND batchno = ? AND modulename = ? AND trainername = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, regNo, batch, module, trainer);
        return count != null && count > 0;
    }

    public record HistoryRow(String batch, String module, String trainer, String entryDate) {}

    public List<HistoryRow> findHistory(String regNo) {
        String sql = "SELECT batchno, modulename, trainername, DATE(entrydate) as entrydate FROM feedbackformdetail WHERE regno = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new HistoryRow(
                rs.getString("batchno"), rs.getString("modulename"), rs.getString("trainername"), rs.getString("entrydate")
        ), regNo);
    }

    public Optional<FeedbackFormData> findExistingForm(String regNo, String batch, String module, String trainer) {
        String sql = "SELECT * FROM feedbackformdetail WHERE regno = ? AND batchno = ? AND modulename = ? AND trainername = ?";
        List<FeedbackFormData> rows = jdbcTemplate.query(sql, (rs, rowNum) -> new FeedbackFormData(
                rs.getString("TP1"), rs.getString("TP2"), rs.getString("TP3"), rs.getString("TP4"), rs.getString("TP5"),
                rs.getString("TP6"), rs.getString("TP7"), rs.getString("TP8"), rs.getString("TP9"),
                rs.getString("TC1"), rs.getString("TC2"), rs.getString("TC3"), rs.getString("TC4"), rs.getString("TC5"),
                rs.getString("TD1"), rs.getString("TD2"), rs.getString("TD3"), rs.getString("TD4"), rs.getString("TD5"), rs.getString("TD6"),
                rs.getString("OR1"),
                rs.getString("TP_comment"), rs.getString("TC_comment"), rs.getString("TD_comment"), rs.getString("OR_comment")
        ), regNo, batch, module, trainer);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public void insert(String regNo, String batch, String module, String trainer, String email, FeedbackFormData f) {
        String sql = "INSERT INTO feedbackformdetail (regno, batchno, modulename, trainername, email, " +
                "TP1, TP2, TP3, TP4, TP5, TP6, TP7, TP8, TP9, " +
                "TC1, TC2, TC3, TC4, TC5, TD1, TD2, TD3, TD4, TD5, TD6, OR1, " +
                "TP_comment, TC_comment, TD_comment, OR_comment) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                regNo, batch, module, trainer, email,
                f.subjectKnowledge(), f.loginHours(), f.interaction(), f.support(), f.qaSession(),
                f.ppt(), f.industryExamples(), f.assessmentEvaluation(), f.projectsEvaluation(),
                f.courseQuality(), f.courseMaterial(), f.labSessions(), f.projectStandard(), f.assessmentContent(),
                f.courseProgress(), f.teamResponse(), f.queriesResolved(), f.feedbackCollected(), f.performanceNotified(), f.improvementAreas(),
                f.overallRating(),
                f.trainerPerformanceComment(), f.techContentComment(), f.trainingDeliveryComment(), f.overallPerformanceComment()
        );
    }

    public void update(String regNo, String batch, String module, String trainer, String email, FeedbackFormData f) {
        String sql = "UPDATE feedbackformdetail SET trainername = ?, email = ?, " +
                "TP1 = ?, TP2 = ?, TP3 = ?, TP4 = ?, TP5 = ?, TP6 = ?, TP7 = ?, TP8 = ?, TP9 = ?, " +
                "TC1 = ?, TC2 = ?, TC3 = ?, TC4 = ?, TC5 = ?, TD1 = ?, TD2 = ?, TD3 = ?, TD4 = ?, TD5 = ?, TD6 = ?, OR1 = ?, " +
                "TP_comment = ?, TC_comment = ?, TD_comment = ?, OR_comment = ? " +
                "WHERE regno = ? AND batchno = ? AND modulename = ? AND trainername = ?";
        jdbcTemplate.update(sql,
                trainer, email,
                f.subjectKnowledge(), f.loginHours(), f.interaction(), f.support(), f.qaSession(),
                f.ppt(), f.industryExamples(), f.assessmentEvaluation(), f.projectsEvaluation(),
                f.courseQuality(), f.courseMaterial(), f.labSessions(), f.projectStandard(), f.assessmentContent(),
                f.courseProgress(), f.teamResponse(), f.queriesResolved(), f.feedbackCollected(), f.performanceNotified(), f.improvementAreas(),
                f.overallRating(),
                f.trainerPerformanceComment(), f.techContentComment(), f.trainingDeliveryComment(), f.overallPerformanceComment(),
                regNo, batch, module, trainer
        );
    }
}
