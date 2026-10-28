import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../../services/dashboard.service';
import {
  MarkComponent as Mark,
  MarkTotals,
  ModuleMarksRow,
  ReportCardService,
} from '../../../services/report-card.service';
import { SkeletonComponent } from '../../../shared/skeleton/skeleton.component';

export interface StudentDetails {
  studentRegistrationNo: string;
  studentName: string;
  program: string;
}

@Component({
  selector: 'app-report-card',
  standalone: true,
  imports: [CommonModule, SkeletonComponent],
  templateUrl: './report-card.component.html',
  styleUrls: ['./report-card.component.css']
})
export class ReportCardComponent implements OnInit {

  student: StudentDetails = {
    studentRegistrationNo: '',
    studentName: '',
    program: ''
  };

  batch = '';
  modules: ModuleMarksRow[] = [];
  totals: MarkTotals | null = null;
  loading = true;

  // Matches the grading table printed below the marks, and the bands the
  // backend grades against. Changing one means changing all three.
  gradingSystem = [
    { grade: 'A+ (Excellent)', criteria: '90% and above' },
    { grade: 'A (Good)', criteria: '80% to 89%' },
    { grade: 'B+ (Average)', criteria: '70% to 79%' },
    { grade: 'B (Below Average)', criteria: '60% to 69%' },
    { grade: 'C (Poor)', criteria: '50% to 59%' },
    { grade: 'Fail', criteria: 'Below 50%' }
  ];

  constructor(private dashboardService: DashboardService, private reportCardService: ReportCardService) {}

  ngOnInit(): void {
    this.dashboardService.getProfile().subscribe({
      next: (profile) => {
        this.student.studentRegistrationNo = profile.regNo;
        this.student.studentName = profile.name;
      },
    });

    this.reportCardService.getReportCard().subscribe({
      next: (reportCard) => {
        this.student.program = reportCard.course;
        this.batch = reportCard.batch;
        this.modules = reportCard.modules;
        this.totals = reportCard.totals;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  /** Grade comes from the backend so one set of bands decides it. */
  get finalGrade(): string {
    return this.totals?.grade ?? '-----------';
  }

  /** A component the batch never conducted prints as a dash, never as 0. */
  cell(mark: Mark): string {
    return mark.conducted ? this.show(mark.obtained) : '—';
  }

  cellMax(mark: Mark): string {
    return mark.conducted ? this.show(mark.max) : '—';
  }

  /**
   * True where the batch set the work but no mark has been entered for this
   * student. It scores zero, so the page says why rather than leaving them to
   * assume they failed it.
   */
  awaitingMark(mark: Mark): boolean {
    return mark.conducted && !mark.recorded;
  }

  /** Marks carry half points; trailing zeros are noise on a printed card. */
  show(value: number | null): string {
    if (value === null || value === undefined) {
      return '—';
    }
    return String(Number(value));
  }
}
