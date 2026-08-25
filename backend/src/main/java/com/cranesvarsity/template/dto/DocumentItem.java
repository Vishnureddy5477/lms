package com.cranesvarsity.template.dto;

/** One row of the My Documents "View Documents" tab — schema placement.student_document. */
public record DocumentItem(int id, String email, String docType, String docLink) {
}
