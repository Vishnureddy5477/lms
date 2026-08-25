package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.StudentDocumentDao;
import com.cranesvarsity.template.dto.DocumentItem;
import com.cranesvarsity.template.model.cranescrm.Admission;
import com.cranesvarsity.template.repository.AdmissionRepository;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/** Backs the My Documents page — successor of legacy manage-documents.jsp / UploadDocument.jsp. */
@Service
public class DocumentService {

    // Confirmed against a real existing record's doc_link — matches the actual legacy
    // UploadDocument.jsp convention (that file wasn't present in the checked-out source
    // to read directly, but this folder name is now verified from production data).
    private static final String S3_FOLDER = "lms/student-document";
    private static final long MAX_SIZE_BYTES = 2L * 1024 * 1024; // 2MB, matches legacy client-side check

    private final AdmissionRepository admissionRepository;
    private final StudentDocumentDao studentDocumentDao;
    private final S3UploadService s3UploadService;

    public DocumentService(AdmissionRepository admissionRepository,
                            StudentDocumentDao studentDocumentDao,
                            S3UploadService s3UploadService) {
        this.admissionRepository = admissionRepository;
        this.studentDocumentDao = studentDocumentDao;
        this.s3UploadService = s3UploadService;
    }

    public List<DocumentItem> list(AuthenticatedStudent student) {
        return studentDocumentDao.findByRegNo(student.regNo());
    }

    public void upload(AuthenticatedStudent student, String docType, MultipartFile file) {
        if (docType == null || docType.isBlank()) {
            throw new DocumentException("Please select a document type.");
        }
        if (file == null || file.isEmpty()) {
            throw new DocumentException("Please select a file to upload.");
        }
        if (!"application/pdf".equals(file.getContentType())) {
            throw new DocumentException("This is not a PDF document.");
        }
        if (file.getSize() >= MAX_SIZE_BYTES) {
            throw new DocumentException("PDF file size must be less than 2MB.");
        }

        Admission admission = admissionRepository.findById(student.regNo())
                .orElseThrow(() -> new IllegalStateException("Student record not found for registration_no=" + student.regNo()));

        String docLink;
        try {
            docLink = s3UploadService.upload(file, S3_FOLDER);
        } catch (IOException e) {
            throw new DocumentException("Failed to upload document. Please try again.");
        }

        studentDocumentDao.insert(admission.getRegistrationNo(), admission.getEmail(), docType, docLink);
    }

    public void delete(AuthenticatedStudent student, int id) {
        String docLink = studentDocumentDao.findDocLinkByIdAndRegNo(id, student.regNo())
                .orElseThrow(() -> new DocumentException("Document not found."));

        if (!s3UploadService.delete(docLink)) {
            throw new DocumentException("Error: File could not be deleted.");
        }
        studentDocumentDao.deleteById(id);
    }
}
