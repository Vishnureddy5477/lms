import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ScheduleService, TodayClassItem, WeekClassItem } from '../../../services/schedule.service';
import { SkeletonComponent } from '../../../shared/skeleton/skeleton.component';

function formatDdMmYyyy(date: Date): string {
  const dd = String(date.getDate()).padStart(2, '0');
  const mm = String(date.getMonth() + 1).padStart(2, '0');
  const yyyy = date.getFullYear();
  return `${dd}-${mm}-${yyyy}`;
}

@Component({
  selector: 'app-class-schedule',
  standalone: true,
  imports: [CommonModule, SkeletonComponent],
  templateUrl: './class-schedule.component.html',
  styleUrls: ['./class-schedule.component.css']
})
export class ClassScheduleComponent implements OnInit {

  todayDate: string;
  weekRange: string;

  todayClasses: TodayClassItem[] = [];
  weekClasses: WeekClassItem[] = [];
  loading = true;

  constructor(private scheduleService: ScheduleService) {
    const today = new Date();
    const weekEnd = new Date(today);
    weekEnd.setDate(weekEnd.getDate() + 7);
    this.todayDate = formatDdMmYyyy(today);
    this.weekRange = `${formatDdMmYyyy(today)} to ${formatDdMmYyyy(weekEnd)}`;
  }

  ngOnInit(): void {
    this.scheduleService.getSchedule().subscribe({
      next: (schedule) => {
        this.todayClasses = schedule.todayClasses;
        this.weekClasses = schedule.weekClasses;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  statusBadgeClass(status: string): string {
    switch (status) {
      case 'Live Now': return 'bg-success';
      case 'Completed': return 'bg-secondary';
      case 'Not Started': return 'bg-warning';
      default: return 'bg-info';
    }
  }

  weekStatusBadgeClass(status: string): string {
    return status === 'Completed' ? 'bg-success' : 'bg-info';
  }
}
