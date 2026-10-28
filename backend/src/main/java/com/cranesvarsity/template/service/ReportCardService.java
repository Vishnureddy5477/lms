package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.BatchModuleDao;
import com.cranesvarsity.template.dao.ManageAssignmentMarksDao;
import com.cranesvarsity.template.dao.ManageLabMarksDao;
import com.cranesvarsity.template.dao.ManageProjectMarksDao;
import com.cranesvarsity.template.dao.ManageTheoryMarksDao;
import com.cranesvarsity.template.dao.ModuleTestResultDao;
import com.cranesvarsity.template.dto.MarkComponent;
import com.cranesvarsity.template.dto.MarkTotals;
import com.cranesvarsity.template.dto.ModuleMarksRow;
import com.cranesvarsity.template.dto.ReportCardResponse;
import com.cranesvarsity.template.model.cranescrm.Admission;
import com.cranesvarsity.template.repository.AdmissionRepository;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import com.cranesvarsity.template.util.Marks;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Backs the Report Card / Marks page — successor of legacy mark-card.jsp.
 *
 * HOW A MODULE IS SCORED
 *
 * The syllabus comes from the batch module mapping, not from attendance.
 * Attendance records classes held, so using it put modules on the card that
 * were never examined and let the denominator differ from student to student,
 * which made two students in one batch impossible to compare.
 *
 * Each module has five components with fixed weights — assignment 10, module
 * test 20, lab 25, theory 25, giving 80 — the same weights the Performance
 * Master Sheet uses. A row shows every component the BATCH conducted, so a
 * module test a student sat and scored 19.5/20 on appears even while the
 * module is unfinished.
 *
 * The FINAL GRADE is narrower: a module enters it only once its MCQ, theory
 * and lab have all been conducted, and is then marked out of the full 80, a
 * missing assignment scoring zero. Partly examined modules are listed but not
 * graded. This is the rule the signed-off card format (2026-10-28) is computed
 * by — for a student with five such modules it reads 260 / 400 / 65%.
 *
 * Marks themselves are read exactly as the Performance Master Sheet reads
 * them: keyed on a lower-cased module name, newest row per module, and not
 * restricted by batch — a module sat under an earlier batch number was still
 * earned.
 *
 * Projects are scored per project_type (1 and 2 worth 25, 3 worth 50), not per
 * module. See ManageProjectMarksDao.
 */
@Service
public class ReportCardService {

    private static final BigDecimal ASSIGNMENT_MAX = new BigDecimal("10");
    private static final BigDecimal MCQ_MAX = new BigDecimal("20");
    private static final BigDecimal LAB_MAX = new BigDecimal("25");
    private static final BigDecimal THEORY_MAX = new BigDecimal("25");
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final AdmissionRepository admissionRepository;
    private final BatchModuleDao batchModuleDao;
    private final ModuleTestResultDao moduleTestResultDao;
    private final ManageTheoryMarksDao theoryMarksDao;
    private final ManageLabMarksDao labMarksDao;
    private final ManageProjectMarksDao projectMarksDao;
    private final ManageAssignmentMarksDao assignmentMarksDao;

    public ReportCardService(AdmissionRepository admissionRepository,
                             BatchModuleDao batchModuleDao,
                             ModuleTestResultDao moduleTestResultDao,
                             ManageTheoryMarksDao theoryMarksDao,
                             ManageLabMarksDao labMarksDao,
                             ManageProjectMarksDao projectMarksDao,
                             ManageAssignmentMarksDao assignmentMarksDao) {
        this.admissionRepository = admissionRepository;
        this.batchModuleDao = batchModuleDao;
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

        String batch = admission.getBatchno() == null ? "" : admission.getBatchno().trim();
        Optional<BatchModuleDao.BatchDims> dims = batchModuleDao.findDims(batch);

        // A batch with no row in generatedbatches has no syllabus to score
        // against. Empty totals keep the page honest — it says it has no marks
        // record rather than inventing one out of attendance.
        if (dims.isEmpty()) {
            return new ReportCardResponse(admission.getCourse(), batch,
                    Collections.emptyList(), emptyTotals());
        }

        List<String> modules = batchModuleDao.mappedModules(dims.get());

        // Twelve queries for the whole card, regardless of how many modules the
        // batch runs. The version this replaces issued five per module inside a
        // loop — 26 round trips for an average student and 132 for the worst —
        // at roughly 309ms each from this backend to its database.
        Set<String> assignmentConducted = assignmentMarksDao.modulesConductedForBatch(batch);
        Set<String> mcqConducted = moduleTestResultDao.modulesConductedForBatch(batch);
        Set<String> labConducted = labMarksDao.modulesConductedForBatch(batch);
        Set<String> theoryConducted = theoryMarksDao.modulesConductedForBatch(batch);
        Set<Integer> projectSlotsConducted = projectMarksDao.slotsConductedForBatch(batch);

        Map<String, BigDecimal> assignmentMarks = assignmentMarksDao.latestObtainedByModule(regNo);
        Map<String, BigDecimal> mcqMarks = moduleTestResultDao.reportCardMarksByModule(regNo);
        Map<String, BigDecimal> labMarks = labMarksDao.latestObtainedByModule(regNo);
        Map<String, BigDecimal> theoryMarks = theoryMarksDao.latestObtainedByModule(regNo);
        Map<Integer, ManageProjectMarksDao.ProjectMark> projectMarks = projectMarksDao.latestObtainedBySlot(regNo);

        List<ModuleMarksRow> rows = new ArrayList<>();
        Running running = new Running();
        int sNo = 0;

        for (String module : modules) {
            String key = Marks.key(module);

            MarkComponent mcq = MarkComponent.of(mcqConducted.contains(key), MCQ_MAX, mcqMarks.get(key));
            MarkComponent theory = MarkComponent.of(theoryConducted.contains(key), THEORY_MAX, theoryMarks.get(key));
            MarkComponent lab = MarkComponent.of(labConducted.contains(key), LAB_MAX, labMarks.get(key));
            MarkComponent assignment =
                    MarkComponent.of(assignmentConducted.contains(key), ASSIGNMENT_MAX, assignmentMarks.get(key));

            // The final grade counts a module only once its MCQ, theory and lab
            // have all been held, and then marks it out of the full 80 — a
            // missing assignment scores zero rather than shrinking the total.
            // This is the rule the signed-off card format is computed by.
            if (mcq.conducted() && theory.conducted() && lab.conducted()) {
                MarkComponent gradedAssignment = assignment.conducted()
                        ? assignment
                        : MarkComponent.of(true, ASSIGNMENT_MAX, null);
                running.add(mcq, theory, lab, gradedAssignment, MarkComponent.NOT_CONDUCTED);
            }

            BigDecimal max = sumMax(mcq, theory, lab, assignment);
            BigDecimal obtained = sumObtained(mcq, theory, lab, assignment);

            rows.add(new ModuleMarksRow(++sNo, module, mcq, theory, lab, assignment,
                    MarkComponent.NOT_CONDUCTED, max, obtained, max.signum() > 0));
        }

        // Project rows come after the modules and reuse the same five columns,
        // so the table keeps one shape.
        for (int slot : List.of(1, 2, 3)) {
            if (!projectSlotsConducted.contains(slot)) {
                continue;
            }
            ManageProjectMarksDao.ProjectMark mark = projectMarks.get(slot);
            BigDecimal weight = ManageProjectMarksDao.SLOT_WEIGHTS.get(slot);
            MarkComponent project =
                    MarkComponent.of(true, weight, mark == null ? null : mark.obtained());

            // Listed on the card but outside the final grade, which the card
            // format computes from module marks alone.
            rows.add(new ModuleMarksRow(++sNo, projectLabel(slot, mark),
                    MarkComponent.NOT_CONDUCTED, MarkComponent.NOT_CONDUCTED,
                    MarkComponent.NOT_CONDUCTED, MarkComponent.NOT_CONDUCTED,
                    project, project.max(), project.obtained(), true));
        }

        return new ReportCardResponse(admission.getCourse(), batch, rows, running.totals());
    }

    private static String projectLabel(int slot, ManageProjectMarksDao.ProjectMark mark) {
        String label = mark == null || mark.label() == null ? "" : mark.label().trim();
        return label.isEmpty() ? "Project " + slot : "Project " + slot + " (" + label + ")";
    }

    private static BigDecimal sumMax(MarkComponent... parts) {
        BigDecimal total = BigDecimal.ZERO;
        for (MarkComponent p : parts) {
            if (p.conducted()) {
                total = total.add(p.max());
            }
        }
        return total;
    }

    private static BigDecimal sumObtained(MarkComponent... parts) {
        BigDecimal total = BigDecimal.ZERO;
        for (MarkComponent p : parts) {
            if (p.conducted()) {
                total = total.add(p.obtained());
            }
        }
        return total;
    }

    /**
     * Grading bands as printed on the card, carried over from mark-card.jsp.
     *
     * These are NOT the Performance Master Sheet bands (Excellent 85 / Very
     * Good 70 / Good 60 / Average 50). Kept deliberately so the report card
     * still agrees with the legend beneath it and with cards already issued.
     * Changing one means changing the other.
     */
    private static String gradeFor(BigDecimal percentage) {
        double p = percentage.doubleValue();
        if (p >= 90) {
            return "A+ (Excellent)";
        }
        if (p >= 80) {
            return "A (Good)";
        }
        if (p >= 70) {
            return "B+ (Average)";
        }
        if (p >= 60) {
            return "B (Below Average)";
        }
        if (p >= 50) {
            return "C (Poor)";
        }
        return "Fail";
    }

    private static MarkTotals emptyTotals() {
        BigDecimal z = BigDecimal.ZERO;
        return new MarkTotals(z, z, z, z, z, z, z, z, z, z, z, z, null, null);
    }

    /** Column-wise running totals, skipping components the batch never conducted. */
    private static final class Running {
        private BigDecimal mcqMax = BigDecimal.ZERO;
        private BigDecimal mcqObtained = BigDecimal.ZERO;
        private BigDecimal theoryMax = BigDecimal.ZERO;
        private BigDecimal theoryObtained = BigDecimal.ZERO;
        private BigDecimal labMax = BigDecimal.ZERO;
        private BigDecimal labObtained = BigDecimal.ZERO;
        private BigDecimal assignmentMax = BigDecimal.ZERO;
        private BigDecimal assignmentObtained = BigDecimal.ZERO;
        private BigDecimal projectMax = BigDecimal.ZERO;
        private BigDecimal projectObtained = BigDecimal.ZERO;

        private void add(MarkComponent mcq, MarkComponent theory, MarkComponent lab,
                         MarkComponent assignment, MarkComponent project) {
            if (mcq.conducted()) {
                mcqMax = mcqMax.add(mcq.max());
                mcqObtained = mcqObtained.add(mcq.obtained());
            }
            if (theory.conducted()) {
                theoryMax = theoryMax.add(theory.max());
                theoryObtained = theoryObtained.add(theory.obtained());
            }
            if (lab.conducted()) {
                labMax = labMax.add(lab.max());
                labObtained = labObtained.add(lab.obtained());
            }
            if (assignment.conducted()) {
                assignmentMax = assignmentMax.add(assignment.max());
                assignmentObtained = assignmentObtained.add(assignment.obtained());
            }
            if (project.conducted()) {
                projectMax = projectMax.add(project.max());
                projectObtained = projectObtained.add(project.obtained());
            }
        }

        private MarkTotals totals() {
            BigDecimal max = mcqMax.add(theoryMax).add(labMax).add(assignmentMax).add(projectMax);
            BigDecimal obtained = mcqObtained.add(theoryObtained).add(labObtained)
                    .add(assignmentObtained).add(projectObtained);

            BigDecimal percentage = null;
            String grade = null;
            if (max.signum() > 0) {
                percentage = obtained.multiply(HUNDRED).divide(max, 2, RoundingMode.HALF_UP);
                grade = gradeFor(percentage);
            }

            return new MarkTotals(mcqMax, mcqObtained, theoryMax, theoryObtained,
                    labMax, labObtained, assignmentMax, assignmentObtained,
                    projectMax, projectObtained, max, obtained, percentage, grade);
        }
    }
}
