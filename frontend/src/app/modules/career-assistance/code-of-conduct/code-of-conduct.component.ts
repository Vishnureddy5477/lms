import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DashboardService } from '../../../services/dashboard.service';
import { PlacementService } from '../../../services/placement.service';

export interface ScoreBracket {
  scoreRange: string;
  packageRange: string;
}

// Same static acknowledgement text as legacy placement-code-of-conduct.jsp's hidden fields.
const PLACEMENT_ASSISTANCE_MESSAGE =
  'If yes, then you are hereby confirming the below assistances and directives; ' +
  'Candidates will not have job location constraints. Candidates must be ready to work in PAN INDIA locations. ' +
  'Interested candidate must show interest (within 24hours) for every company that we post on CRM Portal. ' +
  'Maintaining the attendance and completing the projects/assignments/tests before the deadline. ' +
  'Once a candidate has shown interest for any company, he/she must attend the entire interview process. ' +
  'Failure to participate in the entire interview process will result in the discontinuation of further placement opportunities. ' +
  'Candidates must undergo practice sessions/mock tests before attending every interviews. ' +
  'Failure to participate in the practice sessions/mock tests will result in the discontinuation of further placement opportunities. ' +
  "Client's decision is final in terms and conditions, shortlisting and selecting the candidates. " +
  'Weekly tests will be conducted for the students. Based on the weekly test scores, students will be categorized to attend companies as mentioned below. ' +
  'Student can move up or below in the salary bracket range based on the performance in the weekly assessments. ' +
  'Students who have UG/PG semester gaps, students whose academic CGPAs are lesser than 60, students who have backlogs during their course of study at Cranes Varsity will be provided with a project experience certificate of 6 months to one year.';

const YES_PROJECT_EXPERIENCE_MESSAGE =
  'If yes, then the candidate will work as a Trainee R&D Engineer on academic projects. ' +
  'The Experience Certificate will mention the duration of work and project details. ' +
  'Candidates must maintain attendance and complete all projects/assignments/tests on time. ' +
  'Note: Candidates opting for an Experience Certificate will not be eligible for placement assistance from Cranes Varsity.';

const NO_PROJECT_EXPERIENCE_MESSAGE =
  'If No, then the candidate agrees to the certification process, including attendance and module assessments.';

@Component({
  selector: 'app-code-of-conduct',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './code-of-conduct.component.html',
  styleUrls: ['./code-of-conduct.component.css']
})
export class CodeOfConductComponent implements OnInit {

  studentName = '';

  placementInterest: 'Yes' | 'No' | null = null;
  projectCertInterest: 'Yes' | 'No' | null = null;
  termsAgreed = false;
  signature = '';
  isAccepted = false;
  loading = true;
  submitting = false;

  salaryBrackets: ScoreBracket[] = [
    { scoreRange: 'Above 80', packageRange: '6+ LPA' },
    { scoreRange: '60 - 80', packageRange: '4 LPA - 6 LPA' },
    { scoreRange: '40 - 60', packageRange: '2.5 LPA - 4 LPA' },
    { scoreRange: 'Less than 40', packageRange: 'To Be Discussed' }
  ];

  constructor(private dashboardService: DashboardService, private placementService: PlacementService) {}

  ngOnInit(): void {
    this.dashboardService.getProfile().subscribe({
      next: (profile) => (this.studentName = profile.name),
    });

    this.placementService.getCodeOfConductStatus().subscribe({
      next: (status) => {
        this.isAccepted = status.submitted;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  onPlacementInterestChange(): void {
    this.termsAgreed = false;
    this.projectCertInterest = null;
  }

  submitAcceptance(): void {
    if (!this.placementInterest) {
      alert('Please select whether you are interested in placement assistance.');
      return;
    }

    if (!this.termsAgreed) {
      alert('Please confirm that you agree to the terms and conditions.');
      return;
    }

    if (!this.signature.trim()) {
      alert('Please enter your signature.');
      return;
    }

    let message: string;
    if (this.placementInterest === 'Yes') {
      message = PLACEMENT_ASSISTANCE_MESSAGE;
    } else {
      if (!this.projectCertInterest) {
        alert('Please select the project experience certificate option (Yes/No).');
        return;
      }
      message = this.projectCertInterest === 'Yes' ? YES_PROJECT_EXPERIENCE_MESSAGE : NO_PROJECT_EXPERIENCE_MESSAGE;
    }

    this.submitting = true;
    this.placementService
      .submitCodeOfConduct({
        interested: this.placementInterest,
        projectCertificate: this.placementInterest === 'No' ? this.projectCertInterest : null,
        message,
        acceptance: this.termsAgreed,
        signature: this.signature,
      })
      .subscribe({
        next: () => {
          this.submitting = false;
          this.isAccepted = true;
        },
        error: (err) => {
          this.submitting = false;
          alert(err?.error?.message || 'Failed to submit. Please try again.');
        },
      });
  }

}
