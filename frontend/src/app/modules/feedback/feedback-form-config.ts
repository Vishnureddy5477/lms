export interface CriteriaRow {
  label: string;
  field: string;
}

export interface CriteriaSection {
  title: string;
  commentField: string;
  commentHeight: string;
  rows: CriteriaRow[];
}

export const RATING_OPTIONS: { value: string; label: string }[] = [
  { value: 'very_satisfied', label: 'Very Satisfied' },
  { value: 'satisfied', label: 'Satisfied' },
  { value: 'neutral', label: 'Neutral' },
  { value: 'dissatisfied', label: 'Dissatisfied' },
];

export const FEEDBACK_SECTIONS: CriteriaSection[] = [
  {
    title: "Trainer's Performance",
    commentField: 'trainerPerformanceComment',
    commentHeight: '220px',
    rows: [
      { label: 'Knowledge of the Subject & Lecture delivery', field: 'subjectKnowledge' },
      { label: 'Trainer login hours as per training schedule', field: 'loginHours' },
      { label: 'Participation, Interaction & Communication', field: 'interaction' },
      { label: 'Support outside class hours', field: 'support' },
      { label: 'Conducts Q&A, Doubt clearing session', field: 'qaSession' },
      { label: 'PPT for lectures & follow course outline', field: 'ppt' },
      { label: 'Industry Examples & Interview level Questions', field: 'industryExamples' },
      { label: 'Assessment evaluation', field: 'assessmentEvaluation' },
      { label: 'Projects evaluation', field: 'projectsEvaluation' },
    ],
  },
  {
    title: 'Technical Contents, Assignments & Project',
    commentField: 'techContentComment',
    commentHeight: '120px',
    rows: [
      { label: 'Overall Course Quality', field: 'courseQuality' },
      { label: 'Course Material & relevance', field: 'courseMaterial' },
      { label: 'Lab Sessions / online demo', field: 'labSessions' },
      { label: 'Standard of the Project(s)', field: 'projectStandard' },
      { label: 'Assessment, Content & Videos', field: 'assessmentContent' },
    ],
  },
  {
    title: 'Training Delivery',
    commentField: 'trainingDeliveryComment',
    commentHeight: '150px',
    rows: [
      { label: 'Course progress as per plan', field: 'courseProgress' },
      { label: 'Team responds over phone or email', field: 'teamResponse' },
      { label: 'Queries are heard and resolved within 24 hours', field: 'queriesResolved' },
      { label: 'Module wise feedback collected and discussed', field: 'feedbackCollected' },
      { label: 'Module performance notified on time', field: 'performanceNotified' },
      { label: 'Improvement areas discussed', field: 'improvementAreas' },
    ],
  },
  {
    title: 'Overall Ratings',
    commentField: 'overallPerformanceComment',
    commentHeight: '60px',
    rows: [
      { label: 'Overall Course ratings', field: 'overallRating' },
    ],
  },
];

/** Every rating field across all sections, in order — the full required-fields list (all 21). */
export const ALL_RATING_FIELDS: string[] = FEEDBACK_SECTIONS.flatMap((s) => s.rows.map((r) => r.field));
