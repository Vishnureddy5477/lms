package com.cranesvarsity.template.dto;

import java.util.List;

public record SkillTrackerResponse(
        List<SkillTrackerMcqRow> mcqResults,
        List<SkillTrackerMarksRow> theoryResults,
        List<SkillTrackerMarksRow> labResults,
        List<SkillTrackerMarksRow> projectResults,
        List<SkillTrackerMockRow> programmingMockResults,
        List<SkillTrackerMockRow> hardwareMockResults
) {
}
