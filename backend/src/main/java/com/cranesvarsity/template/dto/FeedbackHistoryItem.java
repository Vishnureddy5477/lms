package com.cranesvarsity.template.dto;

/** One row of the Feedback History list — successor of legacy feedback-form-details.jsp. */
public record FeedbackHistoryItem(int slNo, String batch, String module, String trainer, String givenDate) {
}
