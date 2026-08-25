import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface CourseModuleItem {
  moduleName: string;
  contentUrl: string | null;
  hasContent: boolean;
}

export interface CourseModulesResponse {
  course: string;
  modules: CourseModuleItem[];
}

export interface CourseProgressRow {
  moduleName: string;
  startDate: string;
  endDate: string;
  totalDays: number;
  status: string;
}

@Injectable({ providedIn: 'root' })
export class ProgressService {
  constructor(private http: HttpClient) {}

  getCourseOutline(): Observable<CourseModulesResponse> {
    return this.http.get<CourseModulesResponse>(`${environment.apiUrl}/student/course-outline`);
  }

  getLabManual(): Observable<CourseModulesResponse> {
    return this.http.get<CourseModulesResponse>(`${environment.apiUrl}/student/lab-manual`);
  }

  getCourseProgress(): Observable<CourseProgressRow[]> {
    return this.http.get<CourseProgressRow[]>(`${environment.apiUrl}/student/course-progress`);
  }
}
