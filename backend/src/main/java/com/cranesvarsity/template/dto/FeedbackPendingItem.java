package com.cranesvarsity.template.dto;

/** One row of the Submit Feedback list — successor of legacy feedback-form-list.jsp. */
public record FeedbackPendingItem(int slNo, String batch, String module, String trainer, String activatedDate, boolean alreadySubmitted) {
}
