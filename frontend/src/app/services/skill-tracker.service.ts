import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface SkillTrackerMcqRow {
  module: string;
  test: string;
  total: string;
  obtained: string;
  rate: string;
  status: string;
  examDate: string;
  nextExamDate: string;
  attempt: number;
}

export interface SkillTrackerMarksRow {
  module: string;
  totalMarks: number;
  obtainedMarks: number;
  remarks: string;
}

export interface SkillTrackerMockRow {
  batch: string;
  regNo: string;
  participantName: string;
  mockNo: string;
  mockDate: string;
  ratings: string[];
  avgMarks: string;
  result: string;
  evaluatedBy: string;
}

export interface SkillTrackerResponse {
  mcqResults: SkillTrackerMcqRow[];
  theoryResults: SkillTrackerMarksRow[];
  labResults: SkillTrackerMarksRow[];
  projectResults: SkillTrackerMarksRow[];
  programmingMockResults: SkillTrackerMockRow[];
  hardwareMockResults: SkillTrackerMockRow[];
}

@Injectable({ providedIn: 'root' })
export class SkillTrackerService {
  constructor(private http: HttpClient) {}

  getSkillTracker(): Observable<SkillTrackerResponse> {
    return this.http.get<SkillTrackerResponse>(`${environment.apiUrl}/student/skill-tracker`);
  }
}
