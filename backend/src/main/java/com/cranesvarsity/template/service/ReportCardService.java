package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.*;
import com.cranesvarsity.template.dto.MarksPair;
import com.cranesvarsity.template.dto.ModuleMarksRow;
import com.cranesvarsity.template.dto.ReportCardResponse;
import com.cranesvarsity.template.model.cranescrm.Admission;
import com.cranesvarsity.template.repository.AdmissionRepository;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Backs the Report Card / Marks page — successor of legacy mark-card.jsp.
 * Aggregates MCQ (exam_system), Theory (cranescrm), Lab (exam_system),
 * Assignment (exam_system) and Project (cranescrm) marks per module, where
 * the module list is the distinct set of modules the student has attendance
 * for (same source as legacy).
 */
@Service
public class ReportCardService {

    private final AdmissionRepository admissionRepository;
    private final AttendanceDao attendanceDao;
    private final ModuleTestResultDao moduleTestResultDao;
    private final ManageTheoryMarksDao theoryMarksDao;
    private final ManageLabMarksDao labMarksDao;
    private final ManageProjectMarksDao projectMarksDao;
    private final ManageAssignmentMarksDao assignmentMarksDao;

    public ReportCardService(AdmissionRepository admissionRepository,
                              AttendanceDao attendanceDao,
                              ModuleTestResultDao moduleTestResultDao,
                              ManageTheoryMarksDao theoryMarksDao,
                              ManageLabMarksDao labMarksDao,
                              ManageProjectMarksDao projectMarksDao,
                              ManageAssignmentMarksDao assignmentMarksDao) {
        this.admissionRepository = admissionRepository;
        this.attendanceDao = attendanceDao;
        this.moduleTestResultDao = moduleTestResultDao;
        this.theoryMarksDao = theoryMarksDao;
        this.labMarksDao = labMarksDao;
        this.projectMarksDao = projectMarksDao;
        this.assignmentMarksDao = assignmentMarksDao;
    }

    public ReportCardResponse getReportCard(AuthenticatedStudent student) {
        String regNo = student.regNo();

        Admission admission = admissionRepository.findById(regNo)
                .orElseThrow(() -> new IllegalStateException("Student record not found for registration_no=" + regNo));

        List<String> modules = attendanceDao.findDistinctModules(regNo);
        List<ModuleMarksRow> rows = new ArrayList<>();

        for (String module : modules) {
            MarksPair mcq = moduleTestResultDao.getReportCardMarks(regNo, module);
            MarksPair theory = theoryMarksDao.getMarks(regNo, module);
            MarksPair lab = labMarksDao.getMarks(regNo, module);
            MarksPair assignment = assignmentMarksDao.getMarks(regNo, module);
            MarksPair project = projectMarksDao.getMarks(regNo, module);

            int totalMax = mcq.max() + theory.max() + lab.max() + assignment.max() + project.max();
            int totalObtained = mcq.obtained() + theory.obtained() + lab.obtained() + assignment.obtained() + project.obtained();

            rows.add(new ModuleMarksRow(
                    module,
                    mcq.max(), mcq.obtained(),
                    theory.max(), theory.obtained(),
                    lab.max(), lab.obtained(),
                    assignment.max(), assignment.obtained(),
                    project.max(), project.obtained(),
                    totalMax, totalObtained
            ));
        }

        return new ReportCardResponse(admission.getCourse(), rows);
    }
}
