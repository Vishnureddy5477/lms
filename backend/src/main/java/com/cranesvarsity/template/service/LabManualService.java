package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.BooksDao;
import com.cranesvarsity.template.dto.CourseModuleItem;
import com.cranesvarsity.template.dto.CourseModulesResponse;
import com.cranesvarsity.template.model.cranescrm.Admission;
import com.cranesvarsity.template.repository.AdmissionRepository;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Backs the Lab Manual page — successor of legacy view-lab-manual.jsp.
 * Same books table + admission.modules fallback as Course Outline, no B2B
 * variant (legacy has none for lab manuals).
 */
@Service
public class LabManualService {

    private final AdmissionRepository admissionRepository;
    private final BooksDao booksDao;
    private final CourseOutlineService courseOutlineService;

    public LabManualService(AdmissionRepository admissionRepository,
                             BooksDao booksDao,
                             CourseOutlineService courseOutlineService) {
        this.admissionRepository = admissionRepository;
        this.booksDao = booksDao;
        this.courseOutlineService = courseOutlineService;
    }

    public CourseModulesResponse getLabManual(AuthenticatedStudent student) {
        String regNo = student.regNo();
        Admission admission = admissionRepository.findById(regNo)
                .orElseThrow(() -> new IllegalStateException("Student record not found for registration_no=" + regNo));

        List<CourseModuleItem> modules = new ArrayList<>();
        for (BooksDao.BookRow row : booksDao.findByCourseAndType(admission.getCourse(), "Lab Manual")) {
            modules.add(new CourseModuleItem(row.moduleName(), row.content(), true));
        }

        if (modules.isEmpty()) {
            modules.addAll(courseOutlineService.fallbackFromAdmissionModules(regNo));
        }

        return new CourseModulesResponse(admission.getCourse(), modules);
    }
}
