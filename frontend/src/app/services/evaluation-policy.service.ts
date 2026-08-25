import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ModuleEvaluationRow {
  slNo: number;
  types: string;
  modes: string;
  marks: string;
  total: string;
  passMarks: string;
  updateOn: string;
  updatedBy: string;
}

export interface PlacementEvaluationRow {
  slNo: number;
  types: string;
  testName: string;
  modes: string;
  marks: string;
  total: string;
  passMarks: string;
  updateOn: string;
  updatedBy: string;
}

export interface EvaluationPolicyResponse {
  moduleCriteria: ModuleEvaluationRow[];
  placementCriteria: PlacementEvaluationRow[];
}

@Injectable({ providedIn: 'root' })
export class EvaluationPolicyService {
  constructor(private http: HttpClient) {}

  getPolicy(): Observable<EvaluationPolicyResponse> {
    return this.http.get<EvaluationPolicyResponse>(`${environment.apiUrl}/student/evaluation-policy`);
  }
}
