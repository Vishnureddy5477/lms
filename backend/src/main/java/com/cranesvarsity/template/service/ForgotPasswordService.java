package com.cranesvarsity.template.service;

import com.cranesvarsity.template.model.cranescrm.Admission;
import com.cranesvarsity.template.repository.AdmissionRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ported from login.jsp's embedded forgot-password modal flow (sendOtp / verifyOtp /
 * resetPassword ajaxActions). Legacy holds OTP state in HttpSession; this backend is
 * stateless JWT, so an in-memory map keyed by email stands in as the equivalent
 * short-lived server-side state (same lifetime semantics: per-flow, not DB-persisted).
 * SMTP reuses the same working m.outlook.com relay/account as the helpdesk ticket
 * notifications (already verified live) rather than legacy's untested smtp.office365.com
 * host for the same mailbox.
 */
@Service
public class ForgotPasswordService {

    private static final Logger log = LoggerFactory.getLogger(ForgotPasswordService.class);
    private static final long OTP_VALIDITY_MS = 10 * 60 * 1000L; // 10 minutes, matches legacy

    private record OtpState(String otp, long generatedAtMillis, boolean verified) {
    }

    private final Map<String, OtpState> otpStore = new ConcurrentHashMap<>();

    private final AdmissionRepository admissionRepository;
    private final JavaMailSender mailSender;
    private final String fromAddress;

    public ForgotPasswordService(AdmissionRepository admissionRepository,
                                  JavaMailSender mailSender,
                                  @Value("${spring.mail.username}") String fromAddress) {
        this.admissionRepository = admissionRepository;
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public void sendOtp(String email) {
        if (email == null || email.isBlank()) {
            throw new ForgotPasswordException("Please enter your email address");
        }
        String trimmedEmail = email.trim();

        // Only needed to confirm the address exists and to greet them by name,
        // so the newest of their admissions will do. The reset itself is keyed
        // on email and updates every row, which also leaves a student with
        // several enrolments holding one password across all of them.
        Admission admission = admissionRepository.findAllActiveByEmail(trimmedEmail).stream()
                .findFirst()
                .orElseThrow(() -> new ForgotPasswordException("Email address not found in our records"));

        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        otpStore.put(trimmedEmail, new OtpState(otp, Instant.now().toEpochMilli(), false));

        boolean sent = sendOtpEmail(trimmedEmail, otp, admission.getStname());
        if (!sent) {
            throw new ForgotPasswordException("Failed to send OTP. Please try again");
        }
    }

    public void verifyOtp(String email, String otp) {
        if (otp == null || otp.isBlank()) {
            throw new ForgotPasswordException("Please enter the OTP");
        }
        String trimmedEmail = email == null ? "" : email.trim();
        OtpState state = otpStore.get(trimmedEmail);

        if (state == null) {
            throw new ForgotPasswordException("OTP expired. Please request a new one");
        }
        if (Instant.now().toEpochMilli() - state.generatedAtMillis() > OTP_VALIDITY_MS) {
            otpStore.remove(trimmedEmail);
            throw new ForgotPasswordException("OTP expired. Please request a new one");
        }
        if (!otp.trim().equals(state.otp())) {
            throw new ForgotPasswordException("Invalid OTP. Please try again");
        }

        otpStore.put(trimmedEmail, new OtpState(state.otp(), state.generatedAtMillis(), true));
    }

    public void resetPassword(String email, String newPassword, String confirmPassword) {
        String trimmedEmail = email == null ? "" : email.trim();
        OtpState state = otpStore.get(trimmedEmail);

        if (state == null || !state.verified()) {
            throw new ForgotPasswordException("Please verify OTP first");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new ForgotPasswordException("Please enter new password");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new ForgotPasswordException("Passwords do not match");
        }
        if (newPassword.length() < 6) {
            throw new ForgotPasswordException("Password must be at least 6 characters long");
        }

        int rows = admissionRepository.updatePasswordByEmail(trimmedEmail, newPassword);
        if (rows <= 0) {
            throw new ForgotPasswordException("Failed to reset password. Please try again");
        }

        otpStore.remove(trimmedEmail);
    }

    private boolean sendOtpEmail(String toEmail, String otp, String studentName) {
        String subject = "Password Reset OTP - Cranes Student Portal";
        String body = "<html><body>"
                + "<h2>Password Reset Request</h2>"
                + "<p>Dear " + studentName + ",</p>"
                + "<p>You have requested to reset your password for Cranes Student Portal.</p>"
                + "<p>Your OTP is: <strong style='font-size: 18px; color: #007bff;'>" + otp + "</strong></p>"
                + "<p><b>This OTP is valid for 10 minutes only.</b></p>"
                + "<p>If you did not request this password reset, please ignore this email and contact our support team immediately.</p>"
                + "<br>"
                + "<p><b>Best regards,</b><br>"
                + "Training Operations Team<br>"
                + "Cranes Varsity<br>"
                + fromAddress + "<br>"
                + "www.CranesVarsity.com</p>"
                + "</body></html>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            log.warn("Failed to send forgot-password OTP email to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }
}
