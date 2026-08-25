package com.cranesvarsity.template.dto;

import java.util.List;

public record CompanyJobsResponse(boolean isAvailableForPlacement, List<CompanyJobItem> jobs) {
}
