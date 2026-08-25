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
import java.util.Map;

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

        // Five queries for the whole report card, not five per module.
        //
        // This loop used to issue 5 queries per module. With the backend ~309ms
        // from its database (app in ap-south-1, RDS in us-east-2), an average
        // student's 5 modules cost 26 round trips (~8s) and the worst case, 26
        // modules, cost 132 (~41s). It is now a constant 7 regardless of module
        // count.
        //
        // The per-module reduction stays in Java rather than becoming SQL on
        // purpose: these tables hold duplicate rows that disagree (292 theory,
        // 555 MCQ, 10 assignment, 1 lab student/module pairs), so the original
        // "first row wins" / "last total wins" behaviour is load-bearing and a
        // GROUP BY would silently change real students' marks.
        Map<String, MarksPair> mcqByModule = moduleTestResultDao.getReportCardMarksByModule(regNo);
        Map<String, MarksPair> theoryByModule = theoryMarksDao.getMarksByModule(regNo);
        Map<String, MarksPair> labByModule = labMarksDao.getMarksByModule(regNo);
        Map<String, MarksPair> assignmentByModule = assignmentMarksDao.getMarksByModule(regNo);
        Map<String, MarksPair> projectByModule = projectMarksDao.getMarksByModule(regNo);

        List<ModuleMarksRow> rows = new ArrayList<>();

        for (String module : modules) {
            MarksPair mcq = mcqByModule.getOrDefault(module, MarksPair.ZERO);
            MarksPair theory = theoryByModule.getOrDefault(module, MarksPair.ZERO);
            MarksPair lab = labByModule.getOrDefault(module, MarksPair.ZERO);
            MarksPair assignment = assignmentByModule.getOrDefault(module, MarksPair.ZERO);
            MarksPair project = projectByModule.getOrDefault(module, MarksPair.ZERO);

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
