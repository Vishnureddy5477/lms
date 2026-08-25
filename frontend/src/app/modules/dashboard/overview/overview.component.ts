import { Component, ViewChild, ElementRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  DashboardService,
  StudentProfile,
  AttendanceSummaryItem,
  McqPerformanceItem,
  PlacementStats,
} from '../../../services/dashboard.service';

@Component({
  selector: 'app-overview',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './overview.component.html',
  styleUrls: ['./overview.component.css'],
})
export class OverviewComponent implements OnInit {
  @ViewChild('paymentModal') paymentModal!: ElementRef<HTMLDialogElement>;

  profile: StudentProfile | null = null;
  jdStats: PlacementStats = { totalOpportunity: 0, totalApplied: 0, totalLost: 0 };
  attendanceRows: AttendanceSummaryItem[] = [];
  mcqRows: McqPerformanceItem[] = [];

  paymentFormModel = {
    method: '',
    amount: null as number | null,
    date: '',
    remarks: '',
  };
  paymentSubmitting = false;
  paymentMessage = '';
  paymentError = false;

  selectedModule = 'ALL';
  selectedMcqModule = 'ALL';

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.dashboardService.getProfile().subscribe({ next: (p) => (this.profile = p) });
    this.dashboardService.getAttendanceSummary().subscribe({ next: (rows) => (this.attendanceRows = rows) });
    this.dashboardService.getMcqPerformance().subscribe({ next: (rows) => (this.mcqRows = rows) });
    this.dashboardService.getPlacementStats().subscribe({ next: (stats) => (this.jdStats = stats) });
  }

  get attendanceModules(): string[] {
    return this.attendanceRows.map((r) => r.moduleName);
  }

  get mcqModules(): string[] {
    return this.mcqRows.map((r) => r.moduleName);
  }

  get filteredAttendanceRows(): AttendanceSummaryItem[] {
    return this.selectedModule === 'ALL'
      ? this.attendanceRows
      : this.attendanceRows.filter((r) => r.moduleName === this.selectedModule);
  }

  get filteredMcqRows(): McqPerformanceItem[] {
    return this.selectedMcqModule === 'ALL'
      ? this.mcqRows
      : this.mcqRows.filter((r) => r.moduleName === this.selectedMcqModule);
  }

  get mcqTotals() {
    return this.mcqRows.reduce(
      (acc, r) => ({
        totalQuestions: acc.totalQuestions + r.totalQuestions,
        correctCount: acc.correctCount + r.correctCount,
        incorrectCount: acc.incorrectCount + r.incorrectCount,
      }),
      { totalQuestions: 0, correctCount: 0, incorrectCount: 0 }
    );
  }

  get mcqTotalAveragePercent(): number {
    const totals = this.mcqTotals;
    return totals.totalQuestions > 0 ? (totals.correctCount / totals.totalQuestions) * 100 : 0;
  }

  get attendanceTotals() {
    return this.attendanceRows.reduce(
      (acc, r) => ({
        totalClasses: acc.totalClasses + r.totalClasses,
        presentCount: acc.presentCount + r.presentCount,
        absentCount: acc.absentCount + r.absentCount,
      }),
      { totalClasses: 0, presentCount: 0, absentCount: 0 }
    );
  }

  get attendanceTotalAveragePercent(): number {
    const totals = this.attendanceTotals;
    return totals.totalClasses > 0 ? (totals.presentCount / totals.totalClasses) * 100 : 0;
  }

  presentHeightPercent(row: AttendanceSummaryItem): number {
    return row.totalClasses > 0 ? (row.presentCount / row.totalClasses) * 100 : 0;
  }

  absentHeightPercent(row: AttendanceSummaryItem): number {
    return row.totalClasses > 0 ? (row.absentCount / row.totalClasses) * 100 : 0;
  }

  mcqCorrectHeightPercent(row: McqPerformanceItem): number {
    return row.totalQuestions > 0 ? (row.correctCount / row.totalQuestions) * 100 : 0;
  }

  mcqIncorrectHeightPercent(row: McqPerformanceItem): number {
    return row.totalQuestions > 0 ? (row.incorrectCount / row.totalQuestions) * 100 : 0;
  }

  onModuleChange(): void {}

  onMcqModuleChange(): void {}

  openPaymentModal(): void {
    this.paymentMessage = '';
    this.paymentModal.nativeElement.showModal();
  }

  closePaymentModal(): void {
    this.paymentModal.nativeElement.close();
  }

  onSubmitPaymentDetails(): void {
    if (!this.paymentFormModel.method || !this.paymentFormModel.amount || !this.paymentFormModel.date) {
      return;
    }

    this.paymentSubmitting = true;
    this.dashboardService
      .submitPayment({
        paymentMethod: this.paymentFormModel.method,
        amount: String(this.paymentFormModel.amount),
        date: this.paymentFormModel.date,
        remarks: this.paymentFormModel.remarks,
      })
      .subscribe({
        next: (res) => {
          this.paymentSubmitting = false;
          this.paymentError = false;
          this.paymentMessage = res.message;
          this.paymentFormModel = { method: '', amount: null, date: '', remarks: '' };
          this.closePaymentModal();
        },
        error: (err) => {
          this.paymentSubmitting = false;
          this.paymentError = true;
          this.paymentMessage = err?.error?.message || 'Failed to submit payment details. Please try again.';
        },
      });
  }
}
