package com.cranesvarsity.template.dto;

import java.util.List;

/**
 * Mirrors the Angular enrollment-details form model. regno/batchno/course/
 * stname identity comes from the authenticated student's admission record,
 * not this DTO — see EnrollmentController.
 */
public record EnrollmentSubmissionRequest(
        String studentName,
        String dob,
        String fatherName,
        String residenceStd,
        String residenceNumber,
        String parentMobile,
        String parentEmail,

        String presentAddress,
        String presentState,
        String presentCity,
        String presentPin,
        String permanentAddress,
        String permanentState,
        String permanentCity,
        String permanentPin,
        String phoneStd,
        String phoneNumber,
        String linkedIn,
        String studentMobile,
        String studentEmail,
        String email1,
        String skypeId,

        List<EducationRow> educationList,
        String additionalQualification,

        String employerName,
        String designation,
        String areaOfWork,
        String domainTechnology,
        String technicalSkills,
        String currentExperience,
        String totalExperience,
        String employerAddress,
        String workPhone,
        String workEmail,
        String webpageUrl,

        List<PreviousEmployment> previousEmployment,

        String interestedInPlacement,
        boolean declarationAccepted
) {
}
