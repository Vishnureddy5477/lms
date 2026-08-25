package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.PlacementCodeOfConductDao;
import com.cranesvarsity.template.dto.CodeOfConductRequest;
import com.cranesvarsity.template.model.cranescrm.Admission;
import com.cranesvarsity.template.repository.AdmissionRepository;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import org.springframework.stereotype.Service;

/**
 * Backs the Placement Code of Conduct page — successor of legacy
 * placement-code-of-conduct.jsp / post-placement-code-of-conduct.jsp.
 * One-submission-only gate and the interested=Yes/No branch (which fields
 * get "NA") replicated exactly; the acknowledgement message text itself is
 * accepted from the client (as legacy does) rather than duplicated
 * server-side, since it's just the static terms text already shown in the UI.
 */
@Service
public class CodeOfConductService {

    private final AdmissionRepository admissionRepository;
    private final PlacementCodeOfConductDao dao;

    public CodeOfConductService(AdmissionRepository admissionRepository, PlacementCodeOfConductDao dao) {
        this.admissionRepository = admissionRepository;
        this.dao = dao;
    }

    public boolean isSubmitted(AuthenticatedStudent student) {
        return dao.exists(student.regNo());
    }

    public void submit(AuthenticatedStudent student, CodeOfConductRequest request) {
        String regNo = student.regNo();

        if (dao.exists(regNo)) {
            throw new PlacementException("Your code of conduct is not accepted, it's already exist. Thank you...");
        }
        if (request.interested() == null || request.interested().isBlank()) {
            throw new PlacementException("Please select the placement interest option (Yes/No).");
        }
        if (!request.acceptance() || request.signature() == null || request.signature().isBlank()) {
            throw new PlacementException("Please accept the terms and provide your signature.");
        }

        Admission admission = admissionRepository.findById(regNo)
                .orElseThrow(() -> new IllegalStateException("Student record not found for registration_no=" + regNo));

        String message = request.message() != null ? request.message().trim() : "";
        String acceptanceValue = "hereby confirm that I have read, understood, and agree to abide by the above terms and conditions.";

        if ("Yes".equalsIgnoreCase(request.interested())) {
            dao.insert(regNo, admission.getEmail(), "Yes", message, "NA", "NA", acceptanceValue, request.signature());
        } else {
            if (request.projectCertificate() == null || request.projectCertificate().isBlank()) {
                throw new PlacementException("Please select the project experience certificate option (Yes/No).");
            }
            dao.insert(regNo, admission.getEmail(), "No", "NA", request.projectCertificate(), message,
                    acceptanceValue, request.signature());
        }
    }
}
