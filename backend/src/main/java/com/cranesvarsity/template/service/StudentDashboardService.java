package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.AttendanceDao;
import com.cranesvarsity.template.dao.ModuleTestResultDao;
import com.cranesvarsity.template.dao.PlacementDao;
import com.cranesvarsity.template.dao.RetailInvoiceDao;
import com.cranesvarsity.template.dao.StudentDetailsDao;
import com.cranesvarsity.template.dto.AttendanceDetailRecord;
import com.cranesvarsity.template.dto.AttendanceSummaryItem;
import com.cranesvarsity.template.dto.McqPerformanceItem;
import com.cranesvarsity.template.dto.PlacementStatsResponse;
import com.cranesvarsity.template.dto.StudentProfileResponse;
import com.cranesvarsity.template.model.cranescrm.Admission;
import com.cranesvarsity.template.repository.AdmissionRepository;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Backs the Dashboard/Overview page — direct successor of legacy index.jsp. */
@Service
public class StudentDashboardService {

    private final AdmissionRepository admissionRepository;
    private final RetailInvoiceDao retailInvoiceDao;
    private final AttendanceDao attendanceDao;
    private final StudentDetailsDao studentDetailsDao;
    private final PlacementDao placementDao;
    private final ModuleTestResultDao moduleTestResultDao;

    public StudentDashboardService(AdmissionRepository admissionRepository,
                                    RetailInvoiceDao retailInvoiceDao,
                                    AttendanceDao attendanceDao,
                                    StudentDetailsDao studentDetailsDao,
                                    PlacementDao placementDao,
                                    ModuleTestResultDao moduleTestResultDao) {
        this.admissionRepository = admissionRepository;
        this.retailInvoiceDao = retailInvoiceDao;
        this.attendanceDao = attendanceDao;
        this.studentDetailsDao = studentDetailsDao;
        this.placementDao = placementDao;
        this.moduleTestResultDao = moduleTestResultDao;
    }

    public StudentProfileResponse getProfile(AuthenticatedStudent student) {
        Admission admission = requireAdmission(student.regNo());
        double nextDues = retailInvoiceDao.getNextDues(admission.getRegistrationNo());
        LocalDate courseStart = attendanceDao.getEarliestClassDate(admission.getRegistrationNo());

        return new StudentProfileResponse(
                admission.getStname(),
                admission.getEmail(),
                admission.getRegistrationNo(),
                admission.getContact(),
                admission.getBatchno(),
                admission.getCourse(),
                courseStart,
                nextDues
        );
    }

    public List<AttendanceSummaryItem> getAttendanceSummary(AuthenticatedStudent student) {
        return attendanceDao.getModuleSummary(student.regNo());
    }

    public List<AttendanceDetailRecord> getAttendanceDetails(AuthenticatedStudent student) {
        return attendanceDao.findDetailsByRegNo(student.regNo());
    }

    public List<McqPerformanceItem> getMcqPerformance(AuthenticatedStudent student) {
        return moduleTestResultDao.getLatestPerModule(student.regNo());
    }

    /** Zeros out (rather than erroring) when the student isn't placement-eligible, same as the legacy card. */
    public PlacementStatsResponse getPlacementStats(AuthenticatedStudent student) {
        Optional<String> remarks = studentDetailsDao.getRemarks(student.regNo());
        boolean eligible = remarks.isPresent() && isPlacementEligible(remarks.get());
        if (!eligible) {
            return new PlacementStatsResponse(0, 0, 0);
        }

        int total = placementDao.countTotalOpportunities(student.regNo());
        int applied = placementDao.countApplied(student.regNo());
        return new PlacementStatsResponse(total, applied, total - applied);
    }

    private boolean isPlacementEligible(String remarks) {
        return "AVBL".equalsIgnoreCase(remarks) || "Placed".equalsIgnoreCase(remarks);
    }

    Admission requireAdmission(String regNo) {
        return admissionRepository.findById(regNo)
                .orElseThrow(() -> new IllegalStateException("Student record not found for registration_no=" + regNo));
    }
}
