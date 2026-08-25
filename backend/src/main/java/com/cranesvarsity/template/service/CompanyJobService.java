package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.PlacementDao;
import com.cranesvarsity.template.dao.StudentDetailsDao;
import com.cranesvarsity.template.dto.CompanyJobItem;
import com.cranesvarsity.template.dto.CompanyJobsResponse;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Backs the Company Job Descriptions page — successor of legacy
 * company-current-jd-details.jsp. Note this page's placement-eligibility
 * gate is a strict, case-sensitive remarks == "AVBL" check — narrower than
 * the dashboard JD widget's case-insensitive AVBL/Placed gate — replicated
 * exactly rather than reusing that broader check.
 */
@Service
public class CompanyJobService {

    private static final DateTimeFormatter DRIVE_MONTH_IN = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter DRIVE_MONTH_OUT = DateTimeFormatter.ofPattern("MMM, yyyy");
    private static final DateTimeFormatter POSTED_IN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter POSTED_OUT = DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm a");

    private final StudentDetailsDao studentDetailsDao;
    private final PlacementDao placementDao;

    public CompanyJobService(StudentDetailsDao studentDetailsDao, PlacementDao placementDao) {
        this.studentDetailsDao = studentDetailsDao;
        this.placementDao = placementDao;
    }

    public CompanyJobsResponse getOpenJobs(AuthenticatedStudent student) {
        String regNo = student.regNo();
        Optional<String> remarks = studentDetailsDao.getRemarks(regNo);
        boolean available = remarks.isPresent() && "AVBL".equals(remarks.get());

        if (!available) {
            return new CompanyJobsResponse(false, List.of());
        }

        List<CompanyJobItem> jobs = placementDao.findOpenJobs(regNo).stream()
                .map(row -> new CompanyJobItem(
                        row.id(),
                        formatDriveMonth(row.driveMonth()),
                        row.companyName(),
                        row.jobLocation(),
                        row.skills(),
                        row.domain(),
                        row.ctc(),
                        row.jobDescription(),
                        row.aboutCompany(),
                        formatPostedTime(row.postedDateTime()),
                        row.interestStatus()
                ))
                .toList();

        return new CompanyJobsResponse(true, jobs);
    }

    public void submitInterest(AuthenticatedStudent student, long jdPostedId, boolean interested) {
        String regNo = student.regNo();
        if (placementDao.hasDecided(regNo, jdPostedId)) {
            throw new PlacementException("You have already applied!");
        }
        placementDao.updateInterest(regNo, jdPostedId, interested ? "Yes" : "not_intrested");
    }

    private String formatDriveMonth(String raw) {
        if (raw == null || raw.isBlank()) {
            return "NA";
        }
        try {
            LocalDate parsed = LocalDate.parse(raw + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return parsed.format(DRIVE_MONTH_OUT);
        } catch (Exception e) {
            return "NA";
        }
    }

    private String formatPostedTime(String raw) {
        if (raw == null || raw.isBlank()) {
            return "NA";
        }
        try {
            LocalDateTime parsed = LocalDateTime.parse(raw, POSTED_IN);
            return parsed.format(POSTED_OUT);
        } catch (Exception e) {
            return "NA";
        }
    }
}
