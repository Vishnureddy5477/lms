package com.cranesvarsity.template.dto;

/** One row of the Placement Monitor JD list — successor of student-jd-details.jsp's table. */
public record JobApplicationItem(
        String applied,
        String companyName,
        String driveMonth,
        String jobLocation,
        String skills,
        String domain,
        String ctc,
        String jobDescriptionUrl,
        String postedDate
) {
}
