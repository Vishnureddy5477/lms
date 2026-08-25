import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PlacementService } from '../../../services/placement.service';
import { SkeletonRowComponent } from '../../../shared/skeleton/skeleton-row.component';

export interface CompanyJob {
  id: number;
  sNo: number;
  interested: boolean | null;
  driveMonth: string;
  companyName: string;
  jobLocation: string;
  skills: string;
  domain: string;
  ctc: string;
  jobDescriptionUrl?: string;
  aboutCompanyUrl?: string;
}

function toInterested(status: string | null): boolean | null {
  if (status === 'Yes') return true;
  if (status === 'not_intrested') return false;
  return null;
}

@Component({
  selector: 'app-company-jd',
  standalone: true,
  imports: [CommonModule, SkeletonRowComponent],
  templateUrl: './company-jd.component.html',
  styleUrls: ['./company-jd.component.css']
})
export class CompanyJdComponent implements OnInit {

  isAvailableForPlacement = false;
  jobPostings: CompanyJob[] = [];
  loading = true;

  constructor(private placementService: PlacementService) {}

  ngOnInit(): void {
    this.placementService.getOpenJobs().subscribe({
      next: (res) => {
        this.isAvailableForPlacement = res.isAvailableForPlacement;
        this.jobPostings = res.jobs.map((j, i) => ({
          id: j.id,
          sNo: i + 1,
          interested: toInterested(j.interestStatus),
          driveMonth: j.driveMonth,
          companyName: j.companyName,
          jobLocation: j.jobLocation,
          skills: j.skills,
          domain: j.domain,
          ctc: j.ctc,
          jobDescriptionUrl: j.jobDescriptionUrl,
          aboutCompanyUrl: j.aboutCompanyUrl,
        }));
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  setInterest(job: CompanyJob, choice: boolean): void {
    if (job.interested !== null) {
      return;
    }
    const confirmed = confirm(
      choice ? 'Are you sure you want to apply for this job?' : "Are you sure you don't want to apply for this job?"
    );
    if (!confirmed) {
      return;
    }

    this.placementService.submitInterest(job.id, choice).subscribe({
      next: () => {
        job.interested = choice;
      },
      error: (err) => {
        alert(err?.error?.message || 'Something went wrong. Please try again.');
      },
    });
  }

}
