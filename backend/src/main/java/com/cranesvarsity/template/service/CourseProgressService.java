package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.AttendanceDao;
import com.cranesvarsity.template.dao.ModuleStatusDao;
import com.cranesvarsity.template.dto.CourseProgressRow;
import com.cranesvarsity.template.model.cranescrm.Admission;
import com.cranesvarsity.template.repository.AdmissionRepository;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Backs the Course Progress Status page — successor of legacy current-status.jsp. */
@Service
public class CourseProgressService {

    private final AdmissionRepository admissionRepository;
    private final AttendanceDao attendanceDao;
    private final ModuleStatusDao moduleStatusDao;

    public CourseProgressService(AdmissionRepository admissionRepository,
                                  AttendanceDao attendanceDao,
                                  ModuleStatusDao moduleStatusDao) {
        this.admissionRepository = admissionRepository;
        this.attendanceDao = attendanceDao;
        this.moduleStatusDao = moduleStatusDao;
    }

    public List<CourseProgressRow> getProgress(AuthenticatedStudent student) {
        Admission admission = admissionRepository.findById(student.regNo())
                .orElseThrow(() -> new IllegalStateException("Student record not found for registration_no=" + student.regNo()));
        String batch = admission.getBatchno();

        List<CourseProgressRow> rows = new ArrayList<>();
        for (String module : attendanceDao.findDistinctModulesByBatch(batch)) {
            String rawStatus = moduleStatusDao.findStatus(batch, module).orElse("NA");
            String status = "Completed".equalsIgnoreCase(rawStatus) ? "Completed" : "Running";

            AttendanceDao.ModuleDateRange range = attendanceDao.getModuleDateRange(batch, module);
            rows.add(new CourseProgressRow(module, range.startDate(), range.endDate(), range.totalDays(), status));
        }

        return rows;
    }
}
