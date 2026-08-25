package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dto.PaymentRequest;
import com.cranesvarsity.template.model.cranescrm.Admission;
import com.cranesvarsity.template.model.cranescrm.ManagePayment;
import com.cranesvarsity.template.repository.AdmissionRepository;
import com.cranesvarsity.template.repository.ManagePaymentRepository;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Saves a student's payment details into manage_payment (status=pending),
 * same as submit-payment-details.jsp — minus the approval email, which is a
 * deliberate follow-up (needs real SMTP secrets + the approve/reject token
 * endpoints from payment-action.jsp).
 */
@Service
public class PaymentService {

    private final AdmissionRepository admissionRepository;
    private final ManagePaymentRepository managePaymentRepository;

    public PaymentService(AdmissionRepository admissionRepository, ManagePaymentRepository managePaymentRepository) {
        this.admissionRepository = admissionRepository;
        this.managePaymentRepository = managePaymentRepository;
    }

    public void submitPayment(AuthenticatedStudent student, PaymentRequest request) {
        Admission admission = admissionRepository.findById(student.regNo())
                .orElseThrow(() -> new IllegalStateException("Student record not found for registration_no=" + student.regNo()));

        StringBuilder details = new StringBuilder();
        details.append("Amount: ").append(request.amount()).append('\n');
        details.append("Date: ").append(request.date()).append('\n');
        if (request.remarks() != null && !request.remarks().isBlank()) {
            details.append("Remarks: ").append(request.remarks());
        }

        ManagePayment payment = new ManagePayment();
        payment.setStname(admission.getStname());
        payment.setStemail(admission.getEmail());
        payment.setRegno(admission.getRegistrationNo());
        payment.setContact(admission.getContact());
        payment.setBatch(admission.getBatchno());
        payment.setPaymentMethod(request.paymentMethod());
        payment.setPaymentDetails(details.toString());
        payment.setStatus("pending");
        payment.setCreatedAt(LocalDateTime.now());
        payment.setActionToken(UUID.randomUUID().toString().replace("-", ""));

        managePaymentRepository.save(payment);
    }
}
