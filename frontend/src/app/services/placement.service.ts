import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface DocumentItem {
  id: number;
  email: string;
  docType: string;
  docLink: string;
}

export interface JobApplicationItem {
  applied: string;
  companyName: string;
  driveMonth: string;
  jobLocation: string;
  skills: string;
  domain: string;
  ctc: string;
  jobDescriptionUrl: string;
  postedDate: string;
}

export interface PlacementMonitorResponse {
  studentName: string;
  regNo: string;
  course: string;
  contact: string;
  email: string;
  totalOpportunities: number;
  totalApplied: number;
  totalLost: number;
  jobs: JobApplicationItem[];
}

export interface CompanyJobItem {
  id: number;
  driveMonth: string;
  companyName: string;
  jobLocation: string;
  skills: string;
  domain: string;
  ctc: string;
  jobDescriptionUrl: string;
  aboutCompanyUrl: string;
  postedTime: string;
  interestStatus: string | null;
}

export interface CompanyJobsResponse {
  isAvailableForPlacement: boolean;
  jobs: CompanyJobItem[];
}

export interface CodeOfConductRequest {
  interested: string;
  projectCertificate: string | null;
  message: string;
  acceptance: boolean;
  signature: string;
}

@Injectable({ providedIn: 'root' })
export class PlacementService {
  constructor(private http: HttpClient) {}

  // Documents
  listDocuments(): Observable<DocumentItem[]> {
    return this.http.get<DocumentItem[]>(`${environment.apiUrl}/student/documents`);
  }

  uploadDocument(docType: string, file: File): Observable<{ success: boolean; message: string }> {
    const formData = new FormData();
    formData.append('docType', docType);
    formData.append('file', file);
    return this.http.post<{ success: boolean; message: string }>(`${environment.apiUrl}/student/documents`, formData);
  }

  deleteDocument(id: number): Observable<{ success: boolean; message: string }> {
    return this.http.delete<{ success: boolean; message: string }>(`${environment.apiUrl}/student/documents/${id}`);
  }

  // Placement monitor
  getMonitor(): Observable<PlacementMonitorResponse> {
    return this.http.get<PlacementMonitorResponse>(`${environment.apiUrl}/student/placement/monitor`);
  }

  // Company JDs
  getOpenJobs(): Observable<CompanyJobsResponse> {
    return this.http.get<CompanyJobsResponse>(`${environment.apiUrl}/student/placement/jobs`);
  }

  submitInterest(jobId: number, interested: boolean): Observable<{ success: boolean; message: string }> {
    return this.http.post<{ success: boolean; message: string }>(
      `${environment.apiUrl}/student/placement/jobs/${jobId}/interest`,
      { interested }
    );
  }

  // Code of conduct
  getCodeOfConductStatus(): Observable<{ submitted: boolean }> {
    return this.http.get<{ submitted: boolean }>(`${environment.apiUrl}/student/placement/code-of-conduct/status`);
  }

  submitCodeOfConduct(request: CodeOfConductRequest): Observable<{ success: boolean; message: string }> {
    return this.http.post<{ success: boolean; message: string }>(
      `${environment.apiUrl}/student/placement/code-of-conduct`,
      request
    );
  }
}
