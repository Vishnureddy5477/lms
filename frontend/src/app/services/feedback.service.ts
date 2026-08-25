import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface FeedbackPendingItem {
  slNo: number;
  batch: string;
  module: string;
  trainer: string;
  activatedDate: string;
  alreadySubmitted: boolean;
}

export interface FeedbackHistoryItem {
  slNo: number;
  batch: string;
  module: string;
  trainer: string;
  givenDate: string;
}

export interface FeedbackFormData {
  subjectKnowledge: string;
  loginHours: string;
  interaction: string;
  support: string;
  qaSession: string;
  ppt: string;
  industryExamples: string;
  assessmentEvaluation: string;
  projectsEvaluation: string;
  courseQuality: string;
  courseMaterial: string;
  labSessions: string;
  projectStandard: string;
  assessmentContent: string;
  courseProgress: string;
  teamResponse: string;
  queriesResolved: string;
  feedbackCollected: string;
  performanceNotified: string;
  improvementAreas: string;
  overallRating: string;
  trainerPerformanceComment: string;
  techContentComment: string;
  trainingDeliveryComment: string;
  overallPerformanceComment: string;
}

export function emptyFeedbackForm(): FeedbackFormData {
  return {
    subjectKnowledge: '', loginHours: '', interaction: '', support: '', qaSession: '',
    ppt: '', industryExamples: '', assessmentEvaluation: '', projectsEvaluation: '',
    courseQuality: '', courseMaterial: '', labSessions: '', projectStandard: '', assessmentContent: '',
    courseProgress: '', teamResponse: '', queriesResolved: '', feedbackCollected: '', performanceNotified: '', improvementAreas: '',
    overallRating: '',
    trainerPerformanceComment: '', techContentComment: '', trainingDeliveryComment: '', overallPerformanceComment: '',
  };
}

export interface FeedbackSubmitRequest {
  batch: string;
  module: string;
  trainer: string;
  form: FeedbackFormData;
}

@Injectable({ providedIn: 'root' })
export class FeedbackService {
  constructor(private http: HttpClient) {}

  getPending(): Observable<FeedbackPendingItem[]> {
    return this.http.get<FeedbackPendingItem[]>(`${environment.apiUrl}/student/feedback/pending`);
  }

  getHistory(): Observable<FeedbackHistoryItem[]> {
    return this.http.get<FeedbackHistoryItem[]>(`${environment.apiUrl}/student/feedback/history`);
  }

  getForm(batch: string, module: string, trainer: string): Observable<FeedbackFormData | null> {
    return this.http.get<FeedbackFormData | null>(`${environment.apiUrl}/student/feedback/form`, {
      params: { batch, module, trainer },
    });
  }

  submit(request: FeedbackSubmitRequest): Observable<{ success: boolean; message: string }> {
    return this.http.post<{ success: boolean; message: string }>(`${environment.apiUrl}/student/feedback/form`, request);
  }
}
