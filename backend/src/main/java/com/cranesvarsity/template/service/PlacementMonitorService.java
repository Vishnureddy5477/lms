package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.PlacementDao;
import com.cranesvarsity.template.dto.JobApplicationItem;
import com.cranesvarsity.template.dto.PlacementMonitorResponse;
import com.cranesvarsity.template.model.cranescrm.Admission;
import com.cranesvarsity.template.repository.AdmissionRepository;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import org.springframework.stereotype.Service;

import java.util.List;

/** Backs the Placement Monitor page — successor of legacy student-jd-details.jsp. */
@Service
public class PlacementMonitorService {

    private final AdmissionRepository admissionRepository;
    private final PlacementDao placementDao;

    public PlacementMonitorService(AdmissionRepository admissionRepository, PlacementDao placementDao) {
        this.admissionRepository = admissionRepository;
        this.placementDao = placementDao;
    }

    public PlacementMonitorResponse getMonitor(AuthenticatedStudent student) {
        String regNo = student.regNo();
        Admission admission = admissionRepository.findById(regNo)
                .orElseThrow(() -> new IllegalStateException("Student record not found for registration_no=" + regNo));

        int total = placementDao.countTotalOpportunities(regNo);
        int applied = placementDao.countApplied(regNo);

        List<JobApplicationItem> jobs = placementDao.findAssignedJobs(regNo).stream()
                .map(row -> new JobApplicationItem(
                        row.isApplied(), row.companyName(), row.driveMonth(), row.jobLocation(),
                        row.skills(), row.domain(), row.ctc(), row.jobDescription(), row.postedDateTime()
                ))
                .toList();

        return new PlacementMonitorResponse(
                admission.getStname(), admission.getRegistrationNo(), admission.getCourse(),
                admission.getContact(), admission.getEmail(),
                total, applied, total - applied, jobs
        );
    }
}
