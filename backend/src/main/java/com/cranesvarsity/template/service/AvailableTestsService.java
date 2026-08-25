package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.ManageMcqTestDao;
import com.cranesvarsity.template.dto.AvailableTestItem;
import com.cranesvarsity.template.model.cranescrm.Admission;
import com.cranesvarsity.template.repository.AdmissionRepository;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Backs the Quizzes & Tests page — successor of legacy getAvailableTests.jsp. */
@Service
public class AvailableTestsService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final AdmissionRepository admissionRepository;
    private final ManageMcqTestDao dao;

    public AvailableTestsService(AdmissionRepository admissionRepository, ManageMcqTestDao dao) {
        this.admissionRepository = admissionRepository;
        this.dao = dao;
    }

    public List<AvailableTestItem> getAvailableTests(AuthenticatedStudent student) {
        Admission admission = admissionRepository.findById(student.regNo())
                .orElseThrow(() -> new IllegalStateException("Student record not found for registration_no=" + student.regNo()));

        String today = LocalDate.now().format(DATE_FORMAT);
        return dao.findAvailableTests(admission.getRegistrationNo(), admission.getBatchno(), today);
    }
}
