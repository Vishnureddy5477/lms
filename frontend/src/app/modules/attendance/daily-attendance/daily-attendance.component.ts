import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DashboardService } from '../../../services/dashboard.service';
import { SkeletonRowComponent } from '../../../shared/skeleton/skeleton-row.component';

interface ModuleFilter {
  name: string;
  selected: boolean;
}

interface AttendanceRow {
  sNo: number;
  module: string;
  classDate: string;
  attendance: string;
  inTime: string;
  outTime: string;
  trainer: string;
}

function isPresent(status: string): boolean {
  const normalized = (status || '').trim().toUpperCase();
  return normalized === 'PRESENT' || normalized === 'P';
}

@Component({
  selector: 'app-daily-attendance',
  standalone: true,
  imports: [CommonModule, FormsModule, SkeletonRowComponent],
  templateUrl: './daily-attendance.component.html',
  styleUrls: ['./daily-attendance.component.css']
})
export class DailyAttendanceComponent implements OnInit {

  isFilterOpen = false;
  loading = true;

  studentName = '';
  registrationNo = '';
  batchNo = '';

  totalClasses = 0;
  presentClasses = 0;
  attendancePercentage = 0;

  selectedDropdownModule = 'All Modules';
  availableModules: ModuleFilter[] = [];

  allAttendanceList: AttendanceRow[] = [];
  filteredAttendanceList: AttendanceRow[] = [];

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.dashboardService.getProfile().subscribe({
      next: (profile) => {
        this.studentName = profile.name;
        this.registrationNo = profile.regNo;
        this.batchNo = profile.batch;
      },
    });

    this.dashboardService.getAttendanceDetails().subscribe({
      next: (records) => {
        this.allAttendanceList = records.map((r, i) => ({
          sNo: i + 1,
          module: r.module,
          classDate: r.classDate,
          attendance: r.presentAbsent,
          inTime: r.startTime && r.startTime.trim() ? r.startTime : '-',
          outTime: r.endTime && r.endTime.trim() ? r.endTime : '-',
          trainer: r.trainer,
        }));

        const moduleNames = Array.from(new Set(this.allAttendanceList.map((r) => r.module)));
        this.availableModules = moduleNames.map((name) => ({ name, selected: true }));

        this.filteredAttendanceList = [...this.allAttendanceList];
        this.recomputeStats(this.filteredAttendanceList);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  toggleFilterDrawer(): void {
    this.isFilterOpen = !this.isFilterOpen;
  }

  selectAllModules(): void {
    this.selectedDropdownModule = 'All Modules';
    this.availableModules.forEach((m) => (m.selected = true));
    this.applyFilter();
  }

  applyFilter(): void {
    const selectedNames = this.availableModules.filter((m) => m.selected).map((m) => m.name);

    this.filteredAttendanceList = this.allAttendanceList.filter((item) => {
      const matchesDropdown = this.selectedDropdownModule === 'All Modules' || item.module === this.selectedDropdownModule;
      const matchesCheckbox = selectedNames.includes(item.module);
      return matchesDropdown && matchesCheckbox;
    });

    this.recomputeStats(this.filteredAttendanceList);
  }

  isPresentRow(status: string): boolean {
    return isPresent(status);
  }

  private recomputeStats(rows: AttendanceRow[]): void {
    this.totalClasses = rows.length;
    this.presentClasses = rows.filter((r) => isPresent(r.attendance)).length;
    this.attendancePercentage = this.totalClasses > 0
      ? Math.round((this.presentClasses / this.totalClasses) * 1000) / 10
      : 0;
  }
}
