package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.ManageProjectMarksDao;
import com.cranesvarsity.template.dto.ProjectItem;
import com.cranesvarsity.template.model.cranescrm.Admission;
import com.cranesvarsity.template.repository.AdmissionRepository;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Backs the Upload Projects page — successor of legacy manage-projects.jsp / UploadProjectDocument.jsp. */
@Service
public class ProjectUploadService {

    private static final String S3_FOLDER = "lms/student-document";
    private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024; // 10MB, matches legacy client-side check
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** projectTypeCode -> {projectModule (project_module column), projectType (project_type column)}. Copied verbatim from UploadProjectDocument.jsp. */
    private static final Map<String, String[]> PROJECT_TYPES = Map.of(
            "1", new String[]{"Cranes Programming Project", "Project 1"},
            "2", new String[]{"Cranes Hardware Project", "Project 2"},
            "3", new String[]{"Specialization", "Project 3"},
            "4", new String[]{"College Project", "Project 4"},
            "5", new String[]{"Other Project/Internship", "Project 5"}
    );

    private final AdmissionRepository admissionRepository;
    private final ManageProjectMarksDao dao;
    private final S3UploadService s3UploadService;

    public ProjectUploadService(AdmissionRepository admissionRepository, ManageProjectMarksDao dao, S3UploadService s3UploadService) {
        this.admissionRepository = admissionRepository;
        this.dao = dao;
        this.s3UploadService = s3UploadService;
    }

    public List<ProjectItem> list(AuthenticatedStudent student) {
        Admission admission = requireAdmission(student);
        return dao.findByRegNoAndBatch(admission.getRegistrationNo(), admission.getBatchno());
    }

    /** @return true if this updated an existing upload, false if it created a new one. */
    public boolean upload(AuthenticatedStudent student, String projectTypeCode, String projectTitle, MultipartFile file) {
        String[] type = PROJECT_TYPES.get(projectTypeCode);
        if (type == null) {
            throw new ProjectException("Please select a project type.");
        }
        if (projectTitle == null || projectTitle.isBlank()) {
            throw new ProjectException("Please enter a project title.");
        }
        if (file == null || file.isEmpty()) {
            throw new ProjectException("Please select a PDF file to upload.");
        }
        if (!"application/pdf".equals(file.getContentType())) {
            throw new ProjectException("Only PDF files are allowed.");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new ProjectException("File size should not exceed 10MB.");
        }

        String projectModule = type[0];
        String projectType = type[1];

        Admission admission = requireAdmission(student);
        String regNo = admission.getRegistrationNo();
        String batch = admission.getBatchno();
        String now = LocalDateTime.now().format(DATETIME_FORMAT);

        String link;
        try {
            link = s3UploadService.upload(file, S3_FOLDER);
        } catch (IOException e) {
            throw new ProjectException("Failed to upload document. Please try again.");
        }

        Optional<String> existingLink = dao.findExistingLink(regNo, batch, projectModule, projectType);
        if (existingLink.isPresent()) {
            int updated = dao.update(regNo, batch, projectModule, projectType, projectTitle, link, now, admission.getStname());
            if (updated > 0 && existingLink.get() != null && !existingLink.get().isBlank()) {
                s3UploadService.delete(existingLink.get());
            }
            return true;
        } else {
            dao.insert(regNo, batch, projectModule, projectType, projectTitle, link, now, admission.getStname());
            return false;
        }
    }

    private Admission requireAdmission(AuthenticatedStudent student) {
        return admissionRepository.findById(student.regNo())
                .orElseThrow(() -> new IllegalStateException("Student record not found for registration_no=" + student.regNo()));
    }
}
