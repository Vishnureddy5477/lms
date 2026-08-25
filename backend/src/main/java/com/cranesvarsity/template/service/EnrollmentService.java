package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.EnrollmentFormDao;
import com.cranesvarsity.template.dto.EducationRow;
import com.cranesvarsity.template.dto.EnrollmentSubmissionRequest;
import com.cranesvarsity.template.dto.PreviousEmployment;
import com.cranesvarsity.template.model.cranescrm.Admission;
import com.cranesvarsity.template.repository.AdmissionRepository;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Re-implements controller.EnrollmentFormBackUp against the same production
 * "enrollmentform" table: one-time submission gate, S3 photo upload, wide
 * insert, then a sync UPDATE back onto "admission" (parameterized here,
 * unlike the legacy raw string-concat version).
 */
@Service
public class EnrollmentService {

    private static final String S3_FOLDER = "lms/enrollment-documents";

    private final AdmissionRepository admissionRepository;
    private final EnrollmentFormDao enrollmentFormDao;
    private final S3UploadService s3UploadService;

    public EnrollmentService(AdmissionRepository admissionRepository,
                              EnrollmentFormDao enrollmentFormDao,
                              S3UploadService s3UploadService) {
        this.admissionRepository = admissionRepository;
        this.enrollmentFormDao = enrollmentFormDao;
        this.s3UploadService = s3UploadService;
    }

    public boolean isSubmitted(AuthenticatedStudent student) {
        return enrollmentFormDao.existsByRegNo(student.regNo());
    }

    @Transactional
    public void submit(AuthenticatedStudent student, EnrollmentSubmissionRequest request, MultipartFile photo) {
        if (enrollmentFormDao.existsByRegNo(student.regNo())) {
            throw new EnrollmentException("You have already submitted the enrollment form.");
        }
        if (photo == null || photo.isEmpty()) {
            throw new EnrollmentException("Please select a photo.");
        }

        validateRequiredFields(request);

        Admission admission = admissionRepository.findById(student.regNo())
                .orElseThrow(() -> new IllegalStateException("Student record not found for registration_no=" + student.regNo()));

        String photoUrl;
        try {
            photoUrl = s3UploadService.upload(photo, S3_FOLDER);
        } catch (IOException e) {
            throw new EnrollmentException("Failed to upload photo. Please try again.");
        }

        enrollmentFormDao.insert(admission.getRegistrationNo(), admission.getBatchno(), admission.getCourse(), photoUrl, request);

        List<EducationRow> edu = request.educationList();
        EducationRow degree = edu.get(1);
        EducationRow diploma = edu.get(2);
        EducationRow puc = edu.get(3);
        EducationRow tenth = edu.get(4);

        String address = request.permanentAddress() + "," + request.permanentCity() + "," +
                request.permanentState() + "\n" + request.permanentPin();

        admissionRepository.syncFromEnrollment(
                admission.getRegistrationNo(),
                request.studentName(),
                request.studentEmail(),
                address,
                degree.percentage(),
                degree.passoutYear(),
                diploma.percentage(),
                diploma.passoutYear(),
                puc.percentage(),
                puc.passoutYear(),
                tenth.percentage(),
                tenth.passoutYear(),
                request.interestedInPlacement()
        );
    }

    private void validateRequiredFields(EnrollmentSubmissionRequest r) {
        requireNonBlank(r.studentName(), "Student's Name");
        requireNonBlank(r.dob(), "Date Of Birth");
        requireNonBlank(r.fatherName(), "Father's Name");
        requireNonBlank(r.parentMobile(), "Mobile (Parent/Guardian)");
        requireNonBlank(r.presentAddress(), "Present Address");
        requireNonBlank(r.presentState(), "Present State");
        requireNonBlank(r.presentCity(), "Present City");
        requireNonBlank(r.presentPin(), "Present Pin");
        requireNonBlank(r.permanentAddress(), "Permanent Address");
        requireNonBlank(r.permanentState(), "Permanent State");
        requireNonBlank(r.permanentCity(), "Permanent City");
        requireNonBlank(r.permanentPin(), "Permanent Pin");
        requireNonBlank(r.linkedIn(), "Linked-In");
        requireNonBlank(r.studentMobile(), "Mobile (Student)");
        requireNonBlank(r.studentEmail(), "Email (Student)");
        requireNonBlank(r.email1(), "Email1");
        requireNonBlank(r.skypeId(), "Skype Id");
        requireNonBlank(r.interestedInPlacement(), "Interested For Placement");

        if (!r.declarationAccepted()) {
            throw new EnrollmentException("Please accept the declaration before submitting.");
        }
        if (r.educationList() == null || r.educationList().size() != 5) {
            throw new EnrollmentException("Educational qualification details are incomplete.");
        }
        if (r.previousEmployment() == null || r.previousEmployment().size() != 4) {
            throw new EnrollmentException("Previous employment details are incomplete.");
        }
    }

    private void requireNonBlank(String value, String fieldLabel) {
        if (value == null || value.isBlank()) {
            throw new EnrollmentException(fieldLabel + " is required.");
        }
    }
}
