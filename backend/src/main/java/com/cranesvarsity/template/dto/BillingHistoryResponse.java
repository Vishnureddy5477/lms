package com.cranesvarsity.template.dto;

import java.util.List;

public record BillingHistoryResponse(List<BillingRecord> records, double totalDues) {
}
