import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface EducationRow {
  degree: string;
  college: string;
  university: string;
  stream: string;
  passoutYear: string;
  percentage: string;
  gap: string;
  gapYears: string;
}

export interface PreviousEmployment {
  orgName: string;
  designation: string;
  areaOfWork: string;
  from: string;
  to: string;
}

export interface EnrollmentSubmission {
  studentName: string;
  dob: string;
  fatherName: string;
  residenceStd: string;
  residenceNumber: string;
  parentMobile: string;
  parentEmail: string;

  presentAddress: string;
  presentState: string;
  presentCity: string;
  presentPin: string;
  permanentAddress: string;
  permanentState: string;
  permanentCity: string;
  permanentPin: string;
  phoneStd: string;
  phoneNumber: string;
  linkedIn: string;
  studentMobile: string;
  studentEmail: string;
  email1: string;
  skypeId: string;

  educationList: EducationRow[];
  additionalQualification: string;

  employerName: string;
  designation: string;
  areaOfWork: string;
  domainTechnology: string;
  technicalSkills: string;
  currentExperience: string;
  totalExperience: string;
  employerAddress: string;
  workPhone: string;
  workEmail: string;
  webpageUrl: string;

  previousEmployment: PreviousEmployment[];

  interestedInPlacement: string;
  declarationAccepted: boolean;
}

@Injectable({ providedIn: 'root' })
export class EnrollmentService {
  constructor(private http: HttpClient) {}

  getStatus(): Observable<{ submitted: boolean }> {
    return this.http.get<{ submitted: boolean }>(`${environment.apiUrl}/student/enrollment/status`);
  }

  submit(data: EnrollmentSubmission, photo: File): Observable<{ success: boolean; message: string }> {
    const formData = new FormData();
    formData.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' }));
    formData.append('photo', photo);

    return this.http.post<{ success: boolean; message: string }>(
      `${environment.apiUrl}/student/enrollment`,
      formData
    );
  }
}
