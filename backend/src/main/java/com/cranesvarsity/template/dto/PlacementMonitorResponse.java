package com.cranesvarsity.template.dto;

import java.util.List;

public record PlacementMonitorResponse(
        String studentName,
        String regNo,
        String course,
        String contact,
        String email,
        int totalOpportunities,
        int totalApplied,
        int totalLost,
        List<JobApplicationItem> jobs
) {
}
