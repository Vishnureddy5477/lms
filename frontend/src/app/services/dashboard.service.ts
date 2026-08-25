import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface StudentProfile {
  name: string;
  email: string;
  regNo: string;
  contact: string;
  batch: string;
  course: string;
  courseStartDate: string | null;
  nextDues: number;
}

export interface AttendanceSummaryItem {
  moduleName: string;
  totalClasses: number;
  presentCount: number;
  absentCount: number;
  averagePercent: number;
}

export interface AttendanceDetailRecord {
  module: string;
  classDate: string;
  presentAbsent: string;
  startTime: string | null;
  endTime: string | null;
  trainer: string;
}

export interface McqPerformanceItem {
  moduleName: string;
  totalQuestions: number;
  correctCount: number;
  incorrectCount: number;
  averagePercent: string;
  status: string;
}

export interface PlacementStats {
  totalOpportunity: number;
  totalApplied: number;
  totalLost: number;
}

export interface PaymentSubmission {
  paymentMethod: string;
  amount: string;
  date: string;
  remarks: string;
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  constructor(private http: HttpClient) {}

  getProfile(): Observable<StudentProfile> {
    return this.http.get<StudentProfile>(`${environment.apiUrl}/student/me`);
  }

  getAttendanceSummary(): Observable<AttendanceSummaryItem[]> {
    return this.http.get<AttendanceSummaryItem[]>(`${environment.apiUrl}/student/attendance/summary`);
  }

  getAttendanceDetails(): Observable<AttendanceDetailRecord[]> {
    return this.http.get<AttendanceDetailRecord[]>(`${environment.apiUrl}/student/attendance/details`);
  }

  getMcqPerformance(): Observable<McqPerformanceItem[]> {
    return this.http.get<McqPerformanceItem[]>(`${environment.apiUrl}/student/mcq/performance`);
  }

  getPlacementStats(): Observable<PlacementStats> {
    return this.http.get<PlacementStats>(`${environment.apiUrl}/student/placement/stats`);
  }

  submitPayment(payment: PaymentSubmission): Observable<{ success: boolean; message: string }> {
    return this.http.post<{ success: boolean; message: string }>(
      `${environment.apiUrl}/student/payments`,
      payment
    );
  }
}
