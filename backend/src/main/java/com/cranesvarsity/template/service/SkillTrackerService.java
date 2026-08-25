package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.ManageLabMarksDao;
import com.cranesvarsity.template.dao.ManageProgrammingTechnicalMockMarksDao;
import com.cranesvarsity.template.dao.ManageProjectMarksDao;
import com.cranesvarsity.template.dao.ManageTechnicalMockMarksDao;
import com.cranesvarsity.template.dao.ManageTheoryMarksDao;
import com.cranesvarsity.template.dao.ModuleTestResultDao;
import com.cranesvarsity.template.dto.SkillTrackerResponse;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import org.springframework.stereotype.Service;

/**
 * Backs the Skill Tracker page — successor of legacy student-performance.jsp.
 * Pulls MCQ (exam_system), Theory (cranescrm), Lab (exam_system), Project
 * (cranescrm), Programming Mock and Hardware Mock (both exam_system) results
 * for the authenticated student, each tab backed by its own DAO/table.
 */
@Service
public class SkillTrackerService {

    private final ModuleTestResultDao moduleTestResultDao;
    private final ManageTheoryMarksDao theoryMarksDao;
    private final ManageLabMarksDao labMarksDao;
    private final ManageProjectMarksDao projectMarksDao;
    private final ManageProgrammingTechnicalMockMarksDao programmingMockDao;
    private final ManageTechnicalMockMarksDao hardwareMockDao;

    public SkillTrackerService(ModuleTestResultDao moduleTestResultDao,
                                ManageTheoryMarksDao theoryMarksDao,
                                ManageLabMarksDao labMarksDao,
                                ManageProjectMarksDao projectMarksDao,
                                ManageProgrammingTechnicalMockMarksDao programmingMockDao,
                                ManageTechnicalMockMarksDao hardwareMockDao) {
        this.moduleTestResultDao = moduleTestResultDao;
        this.theoryMarksDao = theoryMarksDao;
        this.labMarksDao = labMarksDao;
        this.projectMarksDao = projectMarksDao;
        this.programmingMockDao = programmingMockDao;
        this.hardwareMockDao = hardwareMockDao;
    }

    public SkillTrackerResponse getSkillTracker(AuthenticatedStudent student) {
        String regNo = student.regNo();

        return new SkillTrackerResponse(
                moduleTestResultDao.findAllByRegNo(regNo),
                theoryMarksDao.findAllByRegNo(regNo),
                labMarksDao.findAllByRegNo(regNo),
                projectMarksDao.findMarksByRegNo(regNo),
                programmingMockDao.findByRegNo(regNo),
                hardwareMockDao.findByRegNo(regNo)
        );
    }
}
