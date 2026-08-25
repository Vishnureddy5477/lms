package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.ManageTheoryTestsDao;
import com.cranesvarsity.template.dto.TheoryTestItem;
import com.cranesvarsity.template.model.cranescrm.Admission;
import com.cranesvarsity.template.repository.AdmissionRepository;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import org.springframework.stereotype.Service;

import java.util.List;

/** Backs the Theory/Lab Test page — successor of legacy theory_test_schedule.jsp. */
@Service
public class TheoryTestService {

    private final AdmissionRepository admissionRepository;
    private final ManageTheoryTestsDao dao;

    public TheoryTestService(AdmissionRepository admissionRepository, ManageTheoryTestsDao dao) {
        this.admissionRepository = admissionRepository;
        this.dao = dao;
    }

    public List<TheoryTestItem> getSchedule(AuthenticatedStudent student) {
        Admission admission = admissionRepository.findById(student.regNo())
                .orElseThrow(() -> new IllegalStateException("Student record not found for registration_no=" + student.regNo()));
        return dao.findByBatch(admission.getBatchno());
    }
}
