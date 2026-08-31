import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import {
  FeedbackService,
  FeedbackPendingItem,
  FeedbackHistoryItem,
} from '../../../services/feedback.service';
import { SkeletonRowComponent } from '../../../shared/skeleton/skeleton-row.component';

/** What the student can do with a row, which is also what its button says. */
type FeedbackAction = 'Give Feedback' | 'Edit Feedback' | 'View Feedback';

export interface FeedbackRow {
  slNo: number;
  batch: string;
  module: string;
  trainer: string;
  /** Activated date while pending; the submitted date once given. */
  date: string;
  submitted: boolean;
  /** Still open for editing — false for history-only rows the trainer has closed. */
  editable: boolean;
  action: FeedbackAction;
}

/**
 * The Feedback page — one table for everything, replacing the old
 * "Submit Feedback" and "Feedback History" pair.
 *
 * Those two pages overlapped almost entirely: a module you had already
 * responded to showed up in both, once with an "Edit Feedback" button and once
 * with a "View Feedback" button, and nothing told you they were the same
 * module. Splitting them also meant the answer to "have I done this one?" lived
 * on a different page from the button that does it.
 *
 * So both endpoints are merged here on batch+module+trainer. A module still
 * open for response is editable; one that only survives in history is
 * read-only, because there is no longer an active window to submit into.
 */
@Component({
  selector: 'app-feedback-list',
  standalone: true,
  imports: [CommonModule, SkeletonRowComponent],
  templateUrl: './feedback-list.component.html',
  styleUrls: ['./feedback-list.component.css'],
})
export class FeedbackListComponent implements OnInit {

  rows: FeedbackRow[] = [];
  loading = true;

  constructor(private feedbackService: FeedbackService, private router: Router) {}

  ngOnInit(): void {
    forkJoin({
      pending: this.feedbackService.getPending(),
      history: this.feedbackService.getHistory(),
    }).subscribe({
      next: ({ pending, history }) => {
        this.rows = this.merge(pending, history);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  /**
   * Fold the two lists into one, keyed on the module identity the backend
   * itself uses (batch + module + trainer).
   *
   * Pending rows come first because they are the ones with something to do;
   * history-only rows follow. The submitted date wins over the activated one
   * where both exist — once you have answered, when you answered is the more
   * useful fact.
   */
  private merge(pending: FeedbackPendingItem[], history: FeedbackHistoryItem[]): FeedbackRow[] {
    const keyOf = (batch: string, module: string, trainer: string) =>
      `${batch}||${module}||${trainer}`.toLowerCase();

    const givenDates = new Map<string, string>();
    for (const h of history) {
      givenDates.set(keyOf(h.batch, h.module, h.trainer), h.givenDate);
    }

    const rows: FeedbackRow[] = [];
    const seen = new Set<string>();

    for (const p of pending) {
      const key = keyOf(p.batch, p.module, p.trainer);
      seen.add(key);
      rows.push({
        slNo: 0,
        batch: p.batch,
        module: p.module,
        trainer: p.trainer,
        date: p.alreadySubmitted ? (givenDates.get(key) ?? p.activatedDate) : p.activatedDate,
        submitted: p.alreadySubmitted,
        editable: true,
        action: p.alreadySubmitted ? 'Edit Feedback' : 'Give Feedback',
      });
    }

    // Anything in history that is no longer an open module: readable, not editable.
    for (const h of history) {
      const key = keyOf(h.batch, h.module, h.trainer);
      if (seen.has(key)) {
        continue;
      }
      rows.push({
        slNo: 0,
        batch: h.batch,
        module: h.module,
        trainer: h.trainer,
        date: h.givenDate,
        submitted: true,
        editable: false,
        action: 'View Feedback',
      });
    }

    rows.forEach((row, index) => (row.slNo = index + 1));
    return rows;
  }

  /** Pending first — the rows that still need something are the point of the page. */
  get pendingCount(): number {
    return this.rows.filter((r) => !r.submitted).length;
  }

  openFeedbackForm(row: FeedbackRow): void {
    this.router.navigate(['/feedback/feedback-form'], {
      queryParams: {
        batch: row.batch,
        module: row.module,
        trainer: row.trainer,
        mode: !row.submitted ? 'give' : row.editable ? 'edit' : 'view',
      },
    });
  }
}
