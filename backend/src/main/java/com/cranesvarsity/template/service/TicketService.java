package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.TicketDao;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/** Ported from contact-us.jsp: student ticket submission into student_tickets. */
@Service
public class TicketService {

    private static final String S3_FOLDER = "lms/ticket";
    private static final long MAX_SIZE_BYTES = 16L * 1024 * 1024; // 16MB, matches legacy client-side check

    private final TicketDao ticketDao;
    private final S3UploadService s3UploadService;
    private final TicketNotificationService notificationService;

    public TicketService(TicketDao ticketDao, S3UploadService s3UploadService, TicketNotificationService notificationService) {
        this.ticketDao = ticketDao;
        this.s3UploadService = s3UploadService;
        this.notificationService = notificationService;
    }

    public String submit(AuthenticatedStudent student, String identityOption, String subject,
                          String description, MultipartFile file) {
        if (subject == null || subject.isBlank()) {
            throw new TicketException("Please enter a subject.");
        }
        if (description == null || description.isBlank()) {
            throw new TicketException("Please enter your concern description.");
        }

        String identityType = "Batch Number (Anonymous)".equals(identityOption) ? "batch" : "reg";

        String fileName = null;
        String fileLink = null;
        if (file != null && !file.isEmpty()) {
            if (!"application/pdf".equals(file.getContentType())) {
                throw new TicketException("Please upload a PDF file only.");
            }
            if (file.getSize() > MAX_SIZE_BYTES) {
                throw new TicketException("File size should not exceed 16MB.");
            }
            try {
                fileLink = s3UploadService.upload(file, S3_FOLDER);
                fileName = file.getOriginalFilename();
            } catch (IOException e) {
                throw new TicketException("Failed to upload attachment. Please try again.");
            }
        }

        String ticketId = generateTicketId();
        ticketDao.insert(ticketId, identityType, student.regNo(), student.batch(), subject, description, fileName, fileLink);

        notificationService.sendStaffNotification(ticketId, identityType, student.regNo(), student.batch(), subject, description, fileLink);
        notificationService.sendStudentConfirmation(student.email(), ticketId, subject, description);

        return ticketId;
    }

    private String generateTicketId() {
        String datePart = new SimpleDateFormat("yyyyMMdd").format(new Date());
        int randomNumber = 100000 + new Random().nextInt(900000);
        return datePart + randomNumber;
    }
}
