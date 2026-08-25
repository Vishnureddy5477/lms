package com.cranesvarsity.template.dto;

import java.util.List;

/**
 * One mock-interview evaluation row, shared shape for both Programming and
 * Hardware Mock tabs. {@code ratings} holds TM_1..TM_9 in that order:
 * Communication & Confidence, Resume Standard, Fundamental Concepts,
 * Problem Solving Ability, Coding Skills, Industry Application Awareness,
 * Project Level & Explaining, Technical Competency, Trainers Remark.
 */
public record SkillTrackerMockRow(
        String batch,
        String regNo,
        String participantName,
        String mockNo,
        String mockDate,
        List<String> ratings,
        String avgMarks,
        String result,
        String evaluatedBy
) {
}
