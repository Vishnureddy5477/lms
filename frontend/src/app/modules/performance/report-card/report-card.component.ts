import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../../services/dashboard.service';
import { ReportCardService } from '../../../services/report-card.service';
import { SkeletonComponent } from '../../../shared/skeleton/skeleton.component';

export interface ModuleMark {
  sNo: number;
  moduleName: string;
  mcqMaxMarks: number;
  mcqObtainedMarks: number;
  theoryMaxMarks: number;
  theoryObtainedMarks: number;
  labMaxMarks: number;
  labObtainedMarks: number;
  assignmentMaxMarks: number;
  assignmentObtainedMarks: number;
  projectMaxMarks: number;
  projectObtainedMarks: number;
  totalMaxMarks: number;
  totalObtainedMarks: number;
}

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

  // Left blank/unset — grading here is manual/offline in the legacy system too, not computed.
  finalGrade = '-----------';

  modules: ModuleMark[] = [];
  loading = true;

  // Matches the legacy mark-card.jsp grading table exactly.
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
        this.modules = reportCard.modules.map((m, i) => ({
          sNo: i + 1,
          moduleName: m.moduleName,
          mcqMaxMarks: m.mcqMax,
          mcqObtainedMarks: m.mcqObtained,
          theoryMaxMarks: m.theoryMax,
          theoryObtainedMarks: m.theoryObtained,
          labMaxMarks: m.labMax,
          labObtainedMarks: m.labObtained,
          assignmentMaxMarks: m.assignmentMax,
          assignmentObtainedMarks: m.assignmentObtained,
          projectMaxMarks: m.projectMax,
          projectObtainedMarks: m.projectObtained,
          totalMaxMarks: m.totalMax,
          totalObtainedMarks: m.totalObtained,
        }));
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  // Getters to compute totals across all modules
  get totalMcqMaxMarks(): number { return this.modules.reduce((sum, m) => sum + m.mcqMaxMarks, 0); }
  get totalMcqObtainedMarks(): number { return this.modules.reduce((sum, m) => sum + m.mcqObtainedMarks, 0); }
  get totalTheoryMaxMarks(): number { return this.modules.reduce((sum, m) => sum + m.theoryMaxMarks, 0); }
  get totalTheoryObtainedMarks(): number { return this.modules.reduce((sum, m) => sum + m.theoryObtainedMarks, 0); }
  get totalLabMaxMarks(): number { return this.modules.reduce((sum, m) => sum + m.labMaxMarks, 0); }
  get totalLabObtainedMarks(): number { return this.modules.reduce((sum, m) => sum + m.labObtainedMarks, 0); }
  get totalAssignmentMaxMarks(): number { return this.modules.reduce((sum, m) => sum + m.assignmentMaxMarks, 0); }
  get totalAssignmentObtainedMarks(): number { return this.modules.reduce((sum, m) => sum + m.assignmentObtainedMarks, 0); }
  get totalProjectMaxMarks(): number { return this.modules.reduce((sum, m) => sum + m.projectMaxMarks, 0); }
  get totalProjectObtainedMarks(): number { return this.modules.reduce((sum, m) => sum + m.projectObtainedMarks, 0); }
  get grandTotalMaxMarks(): number { return this.modules.reduce((sum, m) => sum + m.totalMaxMarks, 0); }
  get grandTotalObtainedMarks(): number { return this.modules.reduce((sum, m) => sum + m.totalObtainedMarks, 0); }

}
