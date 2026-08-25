package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dto.ChangePasswordRequest;
import com.cranesvarsity.template.model.cranescrm.Admission;
import com.cranesvarsity.template.repository.AdmissionRepository;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Backs the Change Password tab — successor of legacy change-password.jsp.
 * Same validation order and response-code vocabulary (missing_fields,
 * mismatch, same_password, user_not_found, incorrect_old, update_failed,
 * success) so the Angular UI's existing per-case messaging keeps working.
 * Plaintext compare/update, matching the login flow's already-agreed
 * plaintext approach (see Phase 1 plan) — not re-litigated here.
 */
@Service
public class ChangePasswordService {

    private final AdmissionRepository admissionRepository;

    public ChangePasswordService(AdmissionRepository admissionRepository) {
        this.admissionRepository = admissionRepository;
    }

    public String changePassword(AuthenticatedStudent student, ChangePasswordRequest request) {
        String oldPassword = request.oldPassword();
        String newPassword = request.newPassword();
        String confirmPassword = request.confirmPassword();

        if (isBlank(oldPassword) || isBlank(newPassword) || isBlank(confirmPassword)) {
            return "missing_fields";
        }
        if (!newPassword.equals(confirmPassword)) {
            return "mismatch";
        }
        if (oldPassword.equals(newPassword)) {
            return "same_password";
        }

        Optional<Admission> maybeAdmission = admissionRepository.findActiveByEmail(student.email());
        if (maybeAdmission.isEmpty()) {
            return "user_not_found";
        }
        Admission admission = maybeAdmission.get();

        if (!oldPassword.equals(admission.getPassword())) {
            return "incorrect_old";
        }

        int updated = admissionRepository.updatePassword(admission.getRegistrationNo(), newPassword);
        return updated > 0 ? "success" : "update_failed";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
