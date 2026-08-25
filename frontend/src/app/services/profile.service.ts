import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface PersonalDetails {
  fullName: string;
  email: string;
  phone: string;
  address: string;
  city: string;
  state: string;
}

export interface CourseDetails {
  regNumber: string;
  courseName: string;
  batchNumber: string;
  collegeName: string;
  registrationDate: string;
}

export interface EducationDetailRow {
  slNo: number;
  qualification: string;
  branch: string;
  marks: string;
  yearOfPassing: string;
}

export interface FeesDetailRow {
  slNo: number;
  totalFees: string;
  amountPaid: string;
  dueAmount: string;
  dueDate: string;
  status: string;
}

export interface ProfilePageResponse {
  personal: PersonalDetails;
  course: CourseDetails;
  education: EducationDetailRow[];
  fees: FeesDetailRow[];
}

export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
}

@Injectable({ providedIn: 'root' })
export class ProfileService {
  constructor(private http: HttpClient) {}

  getProfileDetails(): Observable<ProfilePageResponse> {
    return this.http.get<ProfilePageResponse>(`${environment.apiUrl}/student/profile/details`);
  }

  changePassword(request: ChangePasswordRequest): Observable<{ status: string }> {
    return this.http.post<{ status: string }>(`${environment.apiUrl}/student/profile/change-password`, request);
  }
}
