package com.cranesvarsity.template.dto;

import java.util.List;

/** Backs the whole Personal Info page — successor of legacy my-profile.jsp's 4 read-only tabs. */
public record ProfilePageResponse(
        PersonalDetails personal,
        CourseDetails course,
        List<EducationDetailRow> education,
        List<FeesDetailRow> fees
) {
}
