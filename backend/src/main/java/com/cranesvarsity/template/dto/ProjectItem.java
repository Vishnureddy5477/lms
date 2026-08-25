package com.cranesvarsity.template.dto;

/** One row of the Upload Projects page's "My Uploaded Projects" list — schema cranescrm.manage_project_marks. */
public record ProjectItem(
        String type,
        String title,
        String link,
        String module,
        String totalMarks,
        String obtainedMarks,
        String remarks,
        String date
) {
}
