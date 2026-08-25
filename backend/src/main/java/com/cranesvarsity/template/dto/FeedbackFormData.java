package com.cranesvarsity.template.dto;

/**
 * The 21 rating criteria (values: very_satisfied/satisfied/neutral/dissatisfied) + 4 free-text
 * comments — schema cranescrm.feedbackformdetail columns TP1-TP9/TC1-TC5/TD1-TD6/OR1 +
 * TP_comment/TC_comment/TD_comment/OR_comment. Same field set used for submission and for
 * pre-filling the edit/view form.
 */
public record FeedbackFormData(
        // Trainer's Performance (TP1-TP9)
        String subjectKnowledge,
        String loginHours,
        String interaction,
        String support,
        String qaSession,
        String ppt,
        String industryExamples,
        String assessmentEvaluation,
        String projectsEvaluation,
        // Technical Contents, Assignments & Project (TC1-TC5)
        String courseQuality,
        String courseMaterial,
        String labSessions,
        String projectStandard,
        String assessmentContent,
        // Training Delivery (TD1-TD6)
        String courseProgress,
        String teamResponse,
        String queriesResolved,
        String feedbackCollected,
        String performanceNotified,
        String improvementAreas,
        // Overall Rating (OR1)
        String overallRating,
        // Comments
        String trainerPerformanceComment,
        String techContentComment,
        String trainingDeliveryComment,
        String overallPerformanceComment
) {
}
