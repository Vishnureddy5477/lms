package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.EnrollmentFormDao;
import com.cranesvarsity.template.dao.RetailInvoiceDao;
import com.cranesvarsity.template.dto.*;
import com.cranesvarsity.template.model.cranescrm.Admission;
import com.cranesvarsity.template.repository.AdmissionRepository;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Backs the Personal Info page — successor of legacy my-profile.jsp's 4 read-only tabs. */
@Service
public class ProfileService {

    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AdmissionRepository admissionRepository;
    private final EnrollmentFormDao enrollmentFormDao;
    private final RetailInvoiceDao retailInvoiceDao;

    public ProfileService(AdmissionRepository admissionRepository,
                           EnrollmentFormDao enrollmentFormDao,
                           RetailInvoiceDao retailInvoiceDao) {
        this.admissionRepository = admissionRepository;
        this.enrollmentFormDao = enrollmentFormDao;
        this.retailInvoiceDao = retailInvoiceDao;
    }

    public ProfilePageResponse getProfile(AuthenticatedStudent student) {
        Admission admission = admissionRepository.findById(student.regNo())
                .orElseThrow(() -> new IllegalStateException("Student record not found for registration_no=" + student.regNo()));

        PersonalDetails personal = buildPersonalDetails(admission);
        CourseDetails course = buildCourseDetails(admission);
        List<EducationDetailRow> education = buildEducationRows(enrollmentFormDao.findEducationDetails(admission.getRegistrationNo()));
        List<FeesDetailRow> fees = buildFeesRows(retailInvoiceDao.findFeesDetails(admission.getRegistrationNo()));

        return new ProfilePageResponse(personal, course, education, fees);
    }

    private PersonalDetails buildPersonalDetails(Admission admission) {
        // City/State aren't actually tracked anywhere in the legacy schema — my-profile.jsp
        // shows this exact placeholder whenever the student record exists, replicated as-is.
        boolean hasRecord = admission.getStname() != null && !admission.getStname().isBlank();
        String cityStatePlaceholder = hasRecord ? "Available in records" : "Not available";

        return new PersonalDetails(
                admission.getStname(),
                admission.getEmail(),
                nonBlankOr(admission.getContact(), "Not available"),
                nonBlankOr(admission.getAddress(), "Not available"),
                cityStatePlaceholder,
                cityStatePlaceholder
        );
    }

    private CourseDetails buildCourseDetails(Admission admission) {
        String registrationDate = admission.getRegistrationDate() != null
                ? admission.getRegistrationDate().format(DATETIME_FORMAT)
                : "Not available";

        return new CourseDetails(
                nonBlankOr(admission.getRegistrationNo(), "Not available"),
                nonBlankOr(admission.getCourse(), "Not available"),
                nonBlankOr(admission.getBatchno(), "Not available"),
                nonBlankOr(admission.getCollegename(), "Not available"),
                registrationDate
        );
    }

    private List<EducationDetailRow> buildEducationRows(EnrollmentFormDao.EducationDetails e) {
        List<EducationDetailRow> rows = new ArrayList<>();
        rows.add(new EducationDetailRow(1, "Postgraduate (PG)", e.pgBranch(), e.pgMarks(), e.pgYop()));
        rows.add(new EducationDetailRow(2, "Degree", e.degreeBranch(), e.degreeMarks(), e.degreeYop()));
        rows.add(new EducationDetailRow(3, "Diploma", e.diplomaBranch(), e.diplomaMarks(), e.diplomaYop()));
        rows.add(new EducationDetailRow(4, "PUC/12th", e.pucBranch(), e.pucMarks(), e.pucYop()));
        rows.add(new EducationDetailRow(5, "10th Grade", e.tenthBranch(), e.tenthMarks(), e.tenthYop()));
        return rows;
    }

    private List<FeesDetailRow> buildFeesRows(List<RetailInvoiceDao.FeesRow> rows) {
        List<FeesDetailRow> result = new ArrayList<>();
        int slNo = 1;
        for (RetailInvoiceDao.FeesRow row : rows) {
            double paid = row.totalFees() - row.dues();
            boolean pending = row.dues() > 0;
            result.add(new FeesDetailRow(
                    slNo++,
                    formatCurrency(row.totalFees()),
                    formatCurrency(paid),
                    formatCurrency(row.dues()),
                    pending ? row.dueDate() : "N/A",
                    pending ? "Pending" : "Paid"
            ));
        }
        return result;
    }

    private String formatCurrency(double value) {
        return String.format("%.2f", value);
    }

    private String nonBlankOr(String value, String fallback) {
        return (value != null && !value.isBlank()) ? value : fallback;
    }
}
