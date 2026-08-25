import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DashboardService } from '../../../services/dashboard.service';
import { EnrollmentService } from '../../../services/enrollment.service';
import { SkeletonComponent } from '../../../shared/skeleton/skeleton.component';

@Component({
  selector: 'app-enrollment-details',
  standalone: true,
  imports: [CommonModule, FormsModule, SkeletonComponent],
  templateUrl: './enrollment-details.component.html',
  styleUrls: ['./enrollment-details.component.css']
})
export class EnrollmentDetailsComponent implements OnInit {

  enrollmentData = {
    registrationNo: '',
    batchNo: '',
    course: '',
    studentName: '',
    dob: '',
    fatherName: '',
    residenceStd: '',
    residenceNumber: '',
    parentMobile: '',
    parentEmail: '',

    // Contact Information
    presentAddress: '',
    presentState: '',
    presentCity: '',
    presentPin: '',
    permanentAddress: '',
    permanentState: '',
    permanentCity: '',
    permanentPin: '',
    phoneStd: '',
    phoneNumber: '',
    linkedIn: '',
    studentMobile: '',
    studentEmail: '',
    email1: '',
    skypeId: '',

    // Educational Qualification
    educationList: [
      { stage: 'PG', degree: 'None', college: '', university: '', stream: 'None', passoutYear: '', percentage: '', gap: 'No', gapYears: '' },
      { stage: 'DEGREE', degree: 'None', college: '', university: '', stream: 'None', passoutYear: '', percentage: '', gap: 'No', gapYears: '' },
      { stage: 'DIPLOMA', degree: 'diploma', college: '', university: '', stream: '', passoutYear: '', percentage: '', gap: 'No', gapYears: '' },
      { stage: 'PUC/XII STD', degree: 'PUC/10+2/12th', college: '', university: 'College', stream: '', passoutYear: '', percentage: '', gap: 'No', gapYears: '' },
      { stage: 'MATRIC/Xth STD', degree: '10th', college: 'School', university: 'School', stream: '', passoutYear: '', percentage: '', gap: 'No', gapYears: '' }
    ],
    additionalQualification: '',

    // Present Employer
    employerName: '',
    designation: '',
    areaOfWork: '',
    domainTechnology: '',
    technicalSkills: '',
    currentExperience: '',
    totalExperience: '',
    employerAddress: '',
    workPhone: '',
    workEmail: '',
    webpageUrl: '',

    // List of Previous Employment
    previousEmployment: [
      { orgName: '', designation: '', areaOfWork: '', from: '', to: '' },
      { orgName: '', designation: '', areaOfWork: '', from: '', to: '' },
      { orgName: '', designation: '', areaOfWork: '', from: '', to: '' },
      { orgName: '', designation: '', areaOfWork: '', from: '', to: '' }
    ],

    interestedInPlacement: 'Yes',
    declarationAccepted: false
  };

  photoPreview: string | ArrayBuffer | null = null;
  photoFile: File | null = null;

  loading = true;
  alreadySubmitted = false;
  submitting = false;
  message = '';
  isError = false;

  constructor(private dashboardService: DashboardService, private enrollmentService: EnrollmentService) {}

  ngOnInit(): void {
    this.dashboardService.getProfile().subscribe({
      next: (profile) => {
        this.enrollmentData.registrationNo = profile.regNo;
        this.enrollmentData.batchNo = profile.batch;
        this.enrollmentData.course = profile.course;
      },
    });

    this.enrollmentService.getStatus().subscribe({
      next: (status) => {
        this.alreadySubmitted = status.submitted;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  onFileSelected(event: Event): void {
    const target = event.target as HTMLInputElement;
    if (target.files && target.files[0]) {
      const file = target.files[0];
      this.photoFile = file;
      const reader = new FileReader();
      reader.onload = () => (this.photoPreview = reader.result);
      reader.readAsDataURL(file);
    }
  }

  onSubmit(): void {
    this.message = '';

    if (!this.photoFile) {
      this.isError = true;
      this.message = 'Please select a photo.';
      return;
    }

    this.submitting = true;
    this.enrollmentService.submit(this.enrollmentData, this.photoFile).subscribe({
      next: (res) => {
        this.submitting = false;
        this.isError = false;
        this.message = res.message;
        this.alreadySubmitted = true;
      },
      error: (err) => {
        this.submitting = false;
        this.isError = true;
        this.message = err?.error?.message || 'Failed to submit the enrollment form. Please try again.';
      },
    });
  }
}
