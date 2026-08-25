import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FeedbackService, FeedbackPendingItem } from '../../../services/feedback.service';
import { SkeletonRowComponent } from '../../../shared/skeleton/skeleton-row.component';

export interface FeedbackItem {
  slNo: number;
  batchNo: string;
  module: string;
  trainer: string;
  activatedDate: string;
  status: string;
  alreadySubmitted: boolean;
}

@Component({
  selector: 'app-feedback-list',
  standalone: true,
  imports: [CommonModule, SkeletonRowComponent],
  templateUrl: './feedback-list.component.html',
  styleUrls: ['./feedback-list.component.css']
})
export class FeedbackListComponent implements OnInit {

  feedbackList: FeedbackItem[] = [];
  loading = true;

  constructor(private feedbackService: FeedbackService, private router: Router) {}

  ngOnInit(): void {
    this.feedbackService.getPending().subscribe({
      next: (items: FeedbackPendingItem[]) => {
        this.feedbackList = items.map((i) => ({
          slNo: i.slNo,
          batchNo: i.batch,
          module: i.module,
          trainer: i.trainer,
          activatedDate: i.activatedDate,
          status: i.alreadySubmitted ? 'Edit Feedback' : 'Give Feedback',
          alreadySubmitted: i.alreadySubmitted,
        }));
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  openFeedbackForm(item: FeedbackItem): void {
    this.router.navigate(['/feedback/feedback-form'], {
      queryParams: {
        batch: item.batchNo,
        module: item.module,
        trainer: item.trainer,
        mode: item.alreadySubmitted ? 'edit' : 'give',
      },
    });
  }

}
