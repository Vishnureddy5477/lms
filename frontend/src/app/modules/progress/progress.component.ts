import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProgressService, CourseProgressRow } from '../../services/progress.service';
import { SkeletonRowComponent } from '../../shared/skeleton/skeleton-row.component';

interface CourseModuleRow {
  slNo: number;
  moduleName: string;
  contentUrl: string | null;
  hasContent: boolean;
}

interface ProgressRow {
  sn: number;
  moduleName: string;
  startDate: string;
  endDate: string;
  totalDays: number;
  status: string;
}

@Component({
  selector: 'app-progress',
  standalone: true,
  imports: [CommonModule, FormsModule, SkeletonRowComponent],
  templateUrl: './progress.component.html',
  styleUrls: ['./progress.component.css']
})
export class ProgressComponent implements OnInit {

  activeTab: string = 'outline';

  // 1. Course Outline
  courseName = '';
  courseModules: CourseModuleRow[] = [];
  loadingOutline = true;

  // 2. Lab Manual
  labModules: CourseModuleRow[] = [];
  loadingLab = true;

  // 3. Course Progress
  pageSize = 10;
  searchQuery = '';
  currentPage = 1;
  allRows: ProgressRow[] = [];
  loadingProgress = true;

  constructor(private progressService: ProgressService) {}

  ngOnInit(): void {
    this.progressService.getCourseOutline().subscribe({
      next: (res) => {
        this.courseName = res.course;
        this.courseModules = res.modules.map((m, i) => ({
          slNo: i + 1,
          moduleName: m.moduleName,
          contentUrl: m.contentUrl,
          hasContent: m.hasContent,
        }));
        this.loadingOutline = false;
      },
      error: () => { this.loadingOutline = false; },
    });

    this.progressService.getLabManual().subscribe({
      next: (res) => {
        this.courseName = res.course;
        this.labModules = res.modules.map((m, i) => ({
          slNo: i + 1,
          moduleName: m.moduleName,
          contentUrl: m.contentUrl,
          hasContent: m.hasContent,
        }));
        this.loadingLab = false;
      },
      error: () => { this.loadingLab = false; },
    });

    this.progressService.getCourseProgress().subscribe({
      next: (rows: CourseProgressRow[]) => {
        this.allRows = rows.map((r, i) => ({
          sn: i + 1,
          moduleName: r.moduleName,
          startDate: r.startDate,
          endDate: r.endDate,
          totalDays: r.totalDays,
          status: r.status,
        }));
        this.loadingProgress = false;
      },
      error: () => { this.loadingProgress = false; },
    });
  }

  setTab(tab: string): void {
    this.activeTab = tab;
  }

  onViewCourseOutline(moduleItem: CourseModuleRow): void {
    if (moduleItem.contentUrl) {
      window.open(moduleItem.contentUrl, '_blank');
    }
  }

  onViewLabManual(moduleItem: CourseModuleRow): void {
    if (moduleItem.contentUrl) {
      window.open(moduleItem.contentUrl, '_blank');
    }
  }

  get filteredRows(): ProgressRow[] {
    const query = this.searchQuery.trim().toLowerCase();
    if (!query) {
      return this.allRows;
    }
    return this.allRows.filter((r) => r.moduleName.toLowerCase().includes(query));
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredRows.length / this.pageSize));
  }

  get progressList(): ProgressRow[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredRows.slice(start, start + this.pageSize);
  }

  get rangeStart(): number {
    return this.filteredRows.length === 0 ? 0 : (this.currentPage - 1) * this.pageSize + 1;
  }

  get rangeEnd(): number {
    return Math.min(this.currentPage * this.pageSize, this.filteredRows.length);
  }

  prevPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
    }
  }

}
