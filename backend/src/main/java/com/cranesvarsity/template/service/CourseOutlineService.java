package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.BooksDao;
import com.cranesvarsity.template.dao.CourseOutlineContentDao;
import com.cranesvarsity.template.dto.CourseModuleItem;
import com.cranesvarsity.template.dto.CourseModulesResponse;
import com.cranesvarsity.template.model.cranescrm.Admission;
import com.cranesvarsity.template.repository.AdmissionRepository;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Backs the Course Outline page. Legacy sidebar.jsp routes students whose
 * registration_no contains "CL" to the B2B variant
 * (manage_course_outline_content, keyed by batch) and everyone else to the
 * B2C variant (books table, keyed by course) — same branch replicated here.
 * Both fall back to admission.modules (CSV) when no row is found.
 */
@Service
public class CourseOutlineService {

    private final AdmissionRepository admissionRepository;
    private final BooksDao booksDao;
    private final CourseOutlineContentDao courseOutlineContentDao;

    public CourseOutlineService(AdmissionRepository admissionRepository,
                                 BooksDao booksDao,
                                 CourseOutlineContentDao courseOutlineContentDao) {
        this.admissionRepository = admissionRepository;
        this.booksDao = booksDao;
        this.courseOutlineContentDao = courseOutlineContentDao;
    }

    public CourseModulesResponse getOutline(AuthenticatedStudent student) {
        String regNo = student.regNo();
        Admission admission = admissionRepository.findById(regNo)
                .orElseThrow(() -> new IllegalStateException("Student record not found for registration_no=" + regNo));

        List<CourseModuleItem> modules = new ArrayList<>();

        if (regNo.contains("CL")) {
            for (CourseOutlineContentDao.OutlineRow row : courseOutlineContentDao.findByBatch(admission.getBatchno())) {
                modules.add(new CourseModuleItem(row.moduleName(), row.content(), true));
            }
        } else {
            for (BooksDao.BookRow row : booksDao.findByCourseAndType(admission.getCourse(), "Course Outline")) {
                modules.add(new CourseModuleItem(row.moduleName(), row.content(), true));
            }
        }

        if (modules.isEmpty()) {
            modules.addAll(fallbackFromAdmissionModules(regNo));
        }

        return new CourseModulesResponse(admission.getCourse(), modules);
    }

    List<CourseModuleItem> fallbackFromAdmissionModules(String regNo) {
        String csv = admissionRepository.findModulesCsv(regNo);
        List<CourseModuleItem> items = new ArrayList<>();
        if (csv != null && !csv.isBlank() && !csv.equals("Modules,Not,Updated")) {
            for (String name : csv.split(",")) {
                if (!name.trim().isEmpty()) {
                    items.add(new CourseModuleItem(name.trim(), null, false));
                }
            }
        }
        return items;
    }
}
