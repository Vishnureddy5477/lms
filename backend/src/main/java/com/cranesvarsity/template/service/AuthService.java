package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.LoginAuditDao;
import com.cranesvarsity.template.dao.RetailInvoiceDao;
import com.cranesvarsity.template.dto.LoginResponse;
import com.cranesvarsity.template.model.cranescrm.Admission;
import com.cranesvarsity.template.repository.AdmissionRepository;
import com.cranesvarsity.template.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Re-implements the login gate from the legacy login.jsp against the same
 * production "admission" table, faithfully (validity window, course-complete
 * block, dues block, login audit trail) — see the approved plan for why the
 * password check stays plaintext-compare instead of BCrypt for now.
 */
@Service
public class AuthService {

    private final AdmissionRepository admissionRepository;
    private final RetailInvoiceDao retailInvoiceDao;
    private final LoginAuditDao loginAuditDao;
    private final JwtService jwtService;

    public AuthService(AdmissionRepository admissionRepository,
                        RetailInvoiceDao retailInvoiceDao,
                        LoginAuditDao loginAuditDao,
                        JwtService jwtService) {
        this.admissionRepository = admissionRepository;
        this.retailInvoiceDao = retailInvoiceDao;
        this.loginAuditDao = loginAuditDao;
        this.jwtService = jwtService;
    }

    public LoginResponse login(String rawEmail, String rawPassword, HttpServletRequest request) {
        String email = rawEmail.trim();
        String password = rawPassword.trim();

        Optional<Admission> maybeAdmission = admissionRepository.findActiveByEmail(email);
        if (maybeAdmission.isEmpty()) {
            loginAuditDao.logFailure(null, email, "Not Loged In - Invalid Email");
            throw new AuthException("Email address not found. Please check your email and try again.");
        }
        Admission admission = maybeAdmission.get();
        String regNo = admission.getRegistrationNo();

        if (!password.equals(admission.getPassword())) {
            loginAuditDao.logFailure(regNo, email, "Not Loged In - Invalid Password");
            throw new AuthException("Invalid password. Please check your password and try again.");
        }

        boolean validRegistration = admissionRepository.countValidRegistration(email, password) > 0
                || admissionRepository.countOnResumeValid(email, password) > 0;
        if (!validRegistration) {
            loginAuditDao.logFailure(regNo, email, "Not Loged In");
            throw new AuthException("Invalid Email or Password.");
        }

        if (admissionRepository.countCourseCompleted(regNo) > 0) {
            throw new AuthException("Your course is completed. Please contact Training and Delivery.");
        }

        if (retailInvoiceDao.hasOverdueDues(regNo)) {
            throw new AuthException("You have outstanding dues. Please clear them to activate your portal.");
        }

        loginAuditDao.logSuccess(regNo, email, request);

        String token = jwtService.generateToken(admission);
        return new LoginResponse(token, admission.getStname(), admission.getEmail(), regNo,
                admission.getBatchno(), admission.getCourse());
    }
}
