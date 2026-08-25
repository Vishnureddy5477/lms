package com.cranesvarsity.template.service;

import com.cranesvarsity.template.dao.AdmissionFeeDao;
import com.cranesvarsity.template.dto.BillingHistoryResponse;
import com.cranesvarsity.template.dto.BillingRecord;
import com.cranesvarsity.template.security.AuthenticatedStudent;
import org.springframework.stereotype.Service;

import java.util.List;

/** Backs the Billing/Payments page — successor of legacy my-billing-dashboard.jsp. */
@Service
public class BillingService {

    private final AdmissionFeeDao admissionFeeDao;

    public BillingService(AdmissionFeeDao admissionFeeDao) {
        this.admissionFeeDao = admissionFeeDao;
    }

    public BillingHistoryResponse getHistory(AuthenticatedStudent student) {
        List<BillingRecord> records = admissionFeeDao.findByRegNo(student.regNo());
        // Matches legacy's "Total Dues" footer, which is simply the last row's `due` value.
        double totalDues = records.isEmpty() ? 0.0 : records.get(records.size() - 1).due();
        return new BillingHistoryResponse(records, totalDues);
    }
}
