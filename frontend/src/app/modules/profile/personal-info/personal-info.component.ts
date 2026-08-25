import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  ProfileService,
  PersonalDetails,
  CourseDetails,
  EducationDetailRow,
  FeesDetailRow,
} from '../../../services/profile.service';
import { SkeletonComponent } from '../../../shared/skeleton/skeleton.component';

@Component({
  selector: 'app-personal-info',
  standalone: true,
  imports: [CommonModule, FormsModule, SkeletonComponent],
  templateUrl: './personal-info.component.html',
  styleUrls: ['./personal-info.component.css']
})
export class PersonalInfoComponent implements OnInit {

  personalDetails: PersonalDetails = { fullName: '', email: '', phone: '', address: '', city: '', state: '' };
  courseDetails: CourseDetails = { regNumber: '', courseName: '', batchNumber: '', collegeName: '', registrationDate: '' };
  educationalDetails: EducationDetailRow[] = [];
  feesDetails: FeesDetailRow[] = [];
  loading = true;

  passwordMessage = '';
  passwordMessageType: 'success' | 'danger' | 'warning' = 'danger';
  changingPassword = false;

  constructor(private profileService: ProfileService) { }

  ngOnInit(): void {
    this.profileService.getProfileDetails().subscribe({
      next: (res) => {
        this.personalDetails = res.personal;
        this.courseDetails = res.course;
        this.educationalDetails = res.education;
        this.feesDetails = res.fees;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  onChangePassword(current: string, newP: string, confirmP: string, form?: HTMLFormElement): void {
    this.passwordMessage = '';
    this.changingPassword = true;

    this.profileService
      .changePassword({ oldPassword: current, newPassword: newP, confirmPassword: confirmP })
      .subscribe({
        next: (res) => {
          this.changingPassword = false;
          this.applyStatusMessage(res.status, form);
        },
        error: () => {
          this.changingPassword = false;
          this.passwordMessageType = 'danger';
          this.passwordMessage = 'Connection error. Please check your internet connection and try again.';
        },
      });
  }

  private applyStatusMessage(status: string, form?: HTMLFormElement): void {
    switch (status) {
      case 'success':
        this.passwordMessageType = 'success';
        this.passwordMessage = 'Password updated successfully! You can now login with your new password.';
        form?.reset();
        break;
      case 'incorrect_old':
        this.passwordMessageType = 'danger';
        this.passwordMessage = 'Current password is incorrect. Please check and try again.';
        break;
      case 'mismatch':
        this.passwordMessageType = 'warning';
        this.passwordMessage = 'New password and confirm password do not match.';
        break;
      case 'same_password':
        this.passwordMessageType = 'warning';
        this.passwordMessage = 'New password must be different from current password.';
        break;
      case 'missing_fields':
        this.passwordMessageType = 'warning';
        this.passwordMessage = 'Please fill in all password fields.';
        break;
      case 'user_not_found':
        this.passwordMessageType = 'danger';
        this.passwordMessage = 'User account not found. Please contact support.';
        break;
      case 'update_failed':
        this.passwordMessageType = 'danger';
        this.passwordMessage = 'Failed to update password. Please try again.';
        break;
      default:
        this.passwordMessageType = 'danger';
        this.passwordMessage = 'Unexpected response: ' + status;
    }
  }

}
