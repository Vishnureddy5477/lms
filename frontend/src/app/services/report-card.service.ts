import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ModuleMarksRow {
  moduleName: string;
  mcqMax: number;
  mcqObtained: number;
  theoryMax: number;
  theoryObtained: number;
  labMax: number;
  labObtained: number;
  assignmentMax: number;
  assignmentObtained: number;
  projectMax: number;
  projectObtained: number;
  totalMax: number;
  totalObtained: number;
}

export interface ReportCard {
  course: string;
  modules: ModuleMarksRow[];
}

@Injectable({ providedIn: 'root' })
export class ReportCardService {
  constructor(private http: HttpClient) {}

  getReportCard(): Observable<ReportCard> {
    return this.http.get<ReportCard>(`${environment.apiUrl}/student/report-card`);
  }
}
