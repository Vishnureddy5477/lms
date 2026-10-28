import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

/**
 * One mark component of a row.
 *
 * `conducted` false means the batch never ran this component, so the page shows
 * a dash — not a zero, which would read as a mark the student lost.
 * `recorded` false means the batch ran it but no mark exists for this student
 * yet, which does score zero and is worth flagging on screen.
 */
export interface MarkComponent {
  conducted: boolean;
  recorded: boolean;
  max: number | null;
  obtained: number | null;
}

export interface ModuleMarksRow {
  sNo: number;
  moduleName: string;
  mcq: MarkComponent;
  theory: MarkComponent;
  lab: MarkComponent;
  assignment: MarkComponent;
  project: MarkComponent;
  totalMax: number;
  totalObtained: number;
  scored: boolean;
}

export interface MarkTotals {
  mcqMax: number; mcqObtained: number;
  theoryMax: number; theoryObtained: number;
  labMax: number; labObtained: number;
  assignmentMax: number; assignmentObtained: number;
  projectMax: number; projectObtained: number;
  totalMax: number; totalObtained: number;
  percentage: number | null;
  grade: string | null;
}

export interface ReportCard {
  course: string;
  batch: string;
  modules: ModuleMarksRow[];
  totals: MarkTotals;
}

@Injectable({ providedIn: 'root' })
export class ReportCardService {
  constructor(private http: HttpClient) {}

  getReportCard(): Observable<ReportCard> {
    return this.http.get<ReportCard>(`${environment.apiUrl}/student/report-card`);
  }
}
