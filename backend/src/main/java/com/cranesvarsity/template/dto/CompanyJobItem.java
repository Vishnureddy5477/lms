package com.cranesvarsity.template.dto;

/**
 * One row of the Company JD Postings page — successor of
 * company-current-jd-details.jsp. interestStatus is null (no decision yet),
 * "Yes" (applied), or "not_intrested".
 */
public record CompanyJobItem(
        long id,
        String driveMonth,
        String companyName,
        String jobLocation,
        String skills,
        String domain,
        String ctc,
        String jobDescriptionUrl,
        String aboutCompanyUrl,
        String postedTime,
        String interestStatus
) {
}
