import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../services/auth.service';
import { RecordedSessionItem, TutorialsService } from '../../../services/tutorials.service';
import { SkeletonRowComponent } from '../../../shared/skeleton/skeleton-row.component';

@Component({
  selector: 'app-live-sessions-archive',
  standalone: true,
  imports: [CommonModule, SkeletonRowComponent],
  templateUrl: './live-sessions-archive.component.html',
  styleUrls: ['./live-sessions-archive.component.css']
})
export class LiveSessionsArchiveComponent implements OnInit {

  batchName: string = '';
  recordedSessionsList: RecordedSessionItem[] = [];
  loading = true;

  constructor(
    private tutorialsService: TutorialsService,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.batchName = this.authService.getStudent()?.batch || '';
    this.tutorialsService.getRecordedSessions().subscribe({
      next: (list) => {
        this.recordedSessionsList = list;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  openSession(session: RecordedSessionItem): void {
    window.open(session.recordedLink, '_blank');
  }

}
