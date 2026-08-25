import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PlacementService } from '../../../services/placement.service';
import { SkeletonRowComponent } from '../../../shared/skeleton/skeleton-row.component';

export interface JobDescription {
  sNo: number;
  applied: string;
  companyName: string;
  driveMonth: string;
  jobLocation: string;
  skills: string;
  domain: string;
  ctc: string;
  jobDescriptionUrl?: string;
  postedDate: string;
}

@Component({
  selector: 'app-placement-monitor',
  standalone: true,
  imports: [CommonModule, SkeletonRowComponent],
  templateUrl: './placement-monitor.component.html',
  styleUrls: ['./placement-monitor.component.css']
})
export class PlacementMonitorComponent implements OnInit {

  studentInfo = {
    name: '',
    regNo: '',
    course: '',
    contact: '',
    email: ''
  };

  placementStats = {
    totalOpportunities: 0,
    totalApplied: 0,
    totalLost: 0
  };

  jobList: JobDescription[] = [];
  loading = true;

  constructor(private placementService: PlacementService) {}

  ngOnInit(): void {
    this.placementService.getMonitor().subscribe({
      next: (res) => {
        this.studentInfo = {
          name: res.studentName,
          regNo: res.regNo,
          course: res.course,
          contact: res.contact,
          email: res.email,
        };
        this.placementStats = {
          totalOpportunities: res.totalOpportunities,
          totalApplied: res.totalApplied,
          totalLost: res.totalLost,
        };
        this.jobList = res.jobs.map((j, i) => ({
          sNo: i + 1,
          applied: j.applied,
          companyName: j.companyName,
          driveMonth: j.driveMonth,
          jobLocation: j.jobLocation,
          skills: j.skills,
          domain: j.domain,
          ctc: j.ctc,
          jobDescriptionUrl: j.jobDescriptionUrl,
          postedDate: j.postedDate,
        }));
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

}
