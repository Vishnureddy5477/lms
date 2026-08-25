package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.FeedbackFormDao;
import com.cranesvarsity.template.dto.*;
import com.cranesvarsity.template.model.cranescrm.Admission;
import com.cranesvarsity.template.repository.AdmissionRepository;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Backs the Submit Feedback / Feedback History pages — successor of legacy
 * feedback-form-list.jsp, StudentFeedBackFormModuleWise.jsp,
 * EditStudentFeedBack.jsp and feedback-form-details.jsp. Give/Edit are
 * unified into a single upsert here rather than two separate legacy pages.
 */
@Service
public class FeedbackService {

    private static final Logger log = LoggerFactory.getLogger(FeedbackService.class);

    private final AdmissionRepository admissionRepository;
    private final FeedbackFormDao dao;

    public FeedbackService(AdmissionRepository admissionRepository, FeedbackFormDao dao) {
        this.admissionRepository = admissionRepository;
        this.dao = dao;
    }

    /**
     * Fails open by design: a blank/null batch (student's own, or a module_status
     * row's) never counts as a match, and any exception here is swallowed and
     * treated as "nothing pending" — this feature must never lock a student out
     * of their own dashboard. See findPendingModules/findAllByRegNo callers for
     * the same blank-batch guard applied at the query level too.
     */
    public List<FeedbackPendingItem> getPendingFeedback(AuthenticatedStudent student) {
        try {
            Admission admission = requireAdmission(student);
            String batch = admission.getBatchno();
            if (batch == null || batch.isBlank()) {
                return List.of();
            }

            boolean activeAccess = dao.hasActiveModuleFeedbackAccess(admission.getRegistrationNo());

            List<FeedbackPendingItem> items = new ArrayList<>();
            int slNo = 0;
            for (FeedbackFormDao.ModuleStatusRow row : dao.findPendingModules(batch, activeAccess)) {
                slNo++;
                boolean alreadySubmitted = dao.existsFeedback(admission.getRegistrationNo(), batch, row.module(), row.trainer());
                items.add(new FeedbackPendingItem(slNo, batch, row.module(), row.trainer(), row.entryDate(), alreadySubmitted));
            }
            return items;
        } catch (Exception ex) {
            log.warn("Pending-feedback lookup failed for regNo={}; failing open (treating as nothing pending)", student.regNo(), ex);
            return List.of();
        }
    }

    public List<FeedbackHistoryItem> getHistory(AuthenticatedStudent student) {
        List<FeedbackHistoryItem> items = new ArrayList<>();
        int slNo = 0;
        for (FeedbackFormDao.HistoryRow row : dao.findHistory(student.regNo())) {
            slNo++;
            items.add(new FeedbackHistoryItem(slNo, row.batch(), row.module(), row.trainer(), row.entryDate()));
        }
        return items;
    }

    public FeedbackFormData getForm(AuthenticatedStudent student, String batch, String module, String trainer) {
        Optional<FeedbackFormData> existing = dao.findExistingForm(student.regNo(), batch, module, trainer);
        return existing.orElse(null);
    }

    public void submit(AuthenticatedStudent student, FeedbackSubmitRequest request) {
        validate(request);

        Admission admission = requireAdmission(student);
        String regNo = admission.getRegistrationNo();
        String batch = request.batch();
        String module = request.module();
        String trainer = request.trainer();

        if (dao.existsFeedback(regNo, batch, module, trainer)) {
            dao.update(regNo, batch, module, trainer, admission.getEmail(), request.form());
        } else {
            dao.insert(regNo, batch, module, trainer, admission.getEmail(), request.form());
        }
    }

    // All 21 criteria required, confirmed against the actual legacy source.
    private void validate(FeedbackSubmitRequest request) {
        if (request.batch() == null || request.batch().isBlank()
                || request.module() == null || request.module().isBlank()
                || request.trainer() == null || request.trainer().isBlank()) {
            throw new FeedbackException("Missing batch/module/trainer.");
        }

        FeedbackFormData f = request.form();
        if (f == null) {
            throw new FeedbackException("Please select an option for all criteria.");
        }

        String[] required = {
                f.subjectKnowledge(), f.loginHours(), f.interaction(), f.support(), f.qaSession(),
                f.ppt(), f.industryExamples(), f.assessmentEvaluation(), f.projectsEvaluation(),
                f.courseQuality(), f.courseMaterial(), f.labSessions(), f.projectStandard(), f.assessmentContent(),
                f.courseProgress(), f.teamResponse(), f.queriesResolved(), f.feedbackCollected(), f.performanceNotified(), f.improvementAreas(),
                f.overallRating()
        };
        for (String value : required) {
            if (value == null || value.isBlank()) {
                throw new FeedbackException("Please select an option for all criteria.");
            }
        }
    }

    private Admission requireAdmission(AuthenticatedStudent student) {
        return admissionRepository.findById(student.regNo())
                .orElseThrow(() -> new IllegalStateException("Student record not found for registration_no=" + student.regNo()));
    }
}
