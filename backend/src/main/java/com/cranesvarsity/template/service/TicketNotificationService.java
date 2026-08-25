package com.cranesvarsity.template.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Ported from legacy student.RaiseTicket (sendToVP / sendToStudent) — same subject
 * lines and HTML bodies. Staff recipient is vishnukant.reddy@cranesvarsity.com
 * instead of legacy's arpita@cranesvarsity.com (deliberate, per product owner
 * request while this feature is being verified), with no separate CC since the
 * "to" and legacy "cc" now resolve to the same address.
 */
@Service
public class TicketNotificationService {

    private static final Logger log = LoggerFactory.getLogger(TicketNotificationService.class);

    private final JavaMailSender mailSender;
    private final String staffEmail;
    private final String fromAddress;

    public TicketNotificationService(JavaMailSender mailSender,
                                      @Value("${app.helpdesk.staff-email}") String staffEmail,
                                      @Value("${spring.mail.username}") String fromAddress) {
        this.mailSender = mailSender;
        this.staffEmail = staffEmail;
        this.fromAddress = fromAddress;
    }

    public void sendStaffNotification(String ticketId, String identityType, String regNo, String batchNo,
                                       String subject, String description, String fileLink) {
        String identityMode = "reg".equals(identityType) ? "IDENTIFIED" : "ANONYMOUS";
        String finalIdentityValue = "reg".equals(identityType) ? regNo : batchNo;

        String fileAttachmentHtml = "";
        if (fileLink != null && !fileLink.isBlank()) {
            fileAttachmentHtml = "<li><b>Attached File: </b><a href='" + fileLink + "' target='_blank' style='color:#2b6cff; text-decoration:underline;'>View Document</a></li>";
        }

        String emailSubject = "New Student Ticket Raised | Ticket ID- " + ticketId;
        String body = ""
                + "<!DOCTYPE html>"
                + "<html>"
                + "<head><meta charset='UTF-8'></head>"
                + "<body style='font-family: Arial, sans-serif; background-color:#f4f6f8; padding:20px;'>"
                + "<div style='max-width:800px; margin:auto; background:#ffffff; padding:20px; border-radius:6px;'>"
                + "<p>Dear Sir/Madam,</p>"
                + "<p>A new support ticket has been raised by a student through the LMS.</p>"
                + "<p style='margin-top:10px;'><b>Student Subject:</b> " + subject + "</p>"
                + "<p><b>Concern Details:</b></p>"
                + "<ul>"
                + "<li><b>Ticket Id: </b>" + ticketId + "</li>"
                + "<li><b>" + ("IDENTIFIED".equals(identityMode) ? "Registration Number" : "Batch Number") + ":</b> " + finalIdentityValue + "</li>"
                + fileAttachmentHtml
                + "</ul>"
                + "<p><b>Message/Description:</b></p>"
                + "<p style='background:#f9f9f9; padding:10px; border-left:3px solid #2b6cff;'>" + description + "</p>"
                + "<p>Status: <b>OPEN</b></p>"
                + "<p>Please review the concern and take the necessary action.</p>"
                + "<p>Regards,<br><b>LMS Support System</b><br>Cranes Varsity</p>"
                + "</div>"
                + "</body>"
                + "</html>";

        send(staffEmail, emailSubject, body);
    }

    public void sendStudentConfirmation(String studentEmail, String ticketId, String subject, String description) {
        String emailSubject = "Ticket Submitted Successfully | Ticket ID- " + ticketId;
        String body = ""
                + "<!DOCTYPE html>"
                + "<html>"
                + "<head><meta charset='UTF-8'></head>"
                + "<body style='font-family: Arial, sans-serif; background-color:#f4f6f8; padding:20px;'>"
                + "<div style='max-width:800px; margin:auto; background:#ffffff; padding:20px; border-radius:6px;'>"
                + "<h2 style='color:#2b6cff;'>Ticket Submitted Successfully</h2>"
                + "<p>Dear Student,</p>"
                + "<p>Thank you for reaching out to us. Your concern has been successfully submitted.</p>"
                + "<p><b>Ticket ID:</b> " + ticketId + "</p>"
                + "<p><b>Your Subject:</b> " + subject + "</p>"
                + "<p><b>Message/Description:</b></p>"
                + "<p style='background:#f9f9f9; padding:10px; border-left:3px solid #2b6cff;'>" + description + "</p><br>"
                + "<p>Our team will review your concern and get back to you at the earliest.</p>"
                + "<p>Please keep the <b>Ticket ID</b> for future reference.</p>"
                + "<p style='margin-top:20px;'>Best Regards,<br><b>LMS Support Team</b><br>Cranes Varsity</p>"
                + "</div>"
                + "</body>"
                + "</html>";

        send(studentEmail, emailSubject, body);
    }

    /** Best-effort: a notification failure must never roll back or fail the ticket submission itself. */
    private void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send ticket notification email to {}: {}", to, e.getMessage());
        }
    }
}
