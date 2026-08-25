package com.cranesvarsity.template.dto;

/** One row of AdmissionFee — mirrors the fields legacy Printreciept.jsp reads via SELECT *. */
public record BillingRecord(
        int receiptNo,
        String receiptDate,
        String studentName,
        double paid,
        double due,
        String conseller,
        String enquiryNo,
        String course,
        String paymentMode,
        double courseFee,
        int installmentNo,
        double admissionFee,
        String receivedBy,
        String batchNo
) {
}
