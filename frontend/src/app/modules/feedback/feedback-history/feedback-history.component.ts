import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FeedbackService } from '../../../services/feedback.service';
import { SkeletonRowComponent } from '../../../shared/skeleton/skeleton-row.component';

export interface FeedbackHistoryItem {
  slNo: number;
  batch: string;
  module: string;
  trainer: string;
  givenDate: string;
}

@Component({
  selector: 'app-feedback-history',
  standalone: true,
  imports: [CommonModule, SkeletonRowComponent],
  templateUrl: './feedback-history.component.html',
  styleUrls: ['./feedback-history.component.css']
})
export class FeedbackHistoryComponent implements OnInit {

  historyList: FeedbackHistoryItem[] = [];
  loading = true;

  constructor(private feedbackService: FeedbackService, private router: Router) {}

  ngOnInit(): void {
    this.feedbackService.getHistory().subscribe({
      next: (items) => {
        this.historyList = items;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  viewSubmittedFeedback(item: FeedbackHistoryItem): void {
    this.router.navigate(['/feedback/feedback-form'], {
      queryParams: {
        batch: item.batch,
        module: item.module,
        trainer: item.trainer,
        mode: 'view',
      },
    });
  }

}
