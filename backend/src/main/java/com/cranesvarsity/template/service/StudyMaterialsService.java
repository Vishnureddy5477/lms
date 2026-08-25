package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.BooksDao;
import com.cranesvarsity.template.dao.MaterialDownloadLogDao;
import com.cranesvarsity.template.dto.StudyMaterialItem;
import com.cranesvarsity.template.model.cranescrm.Admission;
import com.cranesvarsity.template.repository.AdmissionRepository;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Backs the Study Materials page — successor of legacy materials.jsp. Same
 * books table (contenttype='materials') filtered by course, and the same
 * material_download_log audit trail on download. The legacy download branch
 * also proxies the file through the server with SSL verification disabled —
 * that's a security downgrade, not business logic, so it's not replicated:
 * the frontend opens books.content directly (same URL "View" already uses)
 * and this endpoint's only job is to log the download and hand back that URL.
 */
@Service
public class StudyMaterialsService {

    private static final String CONTENT_TYPE = "materials";

    private final AdmissionRepository admissionRepository;
    private final BooksDao booksDao;
    private final MaterialDownloadLogDao downloadLogDao;

    public StudyMaterialsService(AdmissionRepository admissionRepository, BooksDao booksDao, MaterialDownloadLogDao downloadLogDao) {
        this.admissionRepository = admissionRepository;
        this.booksDao = booksDao;
        this.downloadLogDao = downloadLogDao;
    }

    public List<StudyMaterialItem> list(AuthenticatedStudent student) {
        Admission admission = requireAdmission(student);
        return booksDao.findByCourseAndType(admission.getCourse(), CONTENT_TYPE).stream()
                .map(row -> new StudyMaterialItem(row.id(), row.moduleName(), row.content()))
                .toList();
    }

    /** @return the file URL to open/download, after logging the download audit row. */
    public String recordDownloadAndGetLink(AuthenticatedStudent student, int id) {
        Admission admission = requireAdmission(student);
        BooksDao.BookRow row = booksDao.findByIdAndCourseAndType(id, admission.getCourse(), CONTENT_TYPE)
                .orElseThrow(() -> new StudyMaterialException("Material not found."));
        downloadLogDao.insert(student.regNo(), row.id(), row.moduleName(), CONTENT_TYPE);
        return row.content();
    }

    private Admission requireAdmission(AuthenticatedStudent student) {
        return admissionRepository.findById(student.regNo())
                .orElseThrow(() -> new IllegalStateException("Student record not found for registration_no=" + student.regNo()));
    }
}
