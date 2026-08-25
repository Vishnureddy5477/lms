import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs/operators';
import { FeedbackService, FeedbackPendingItem, emptyFeedbackForm } from '../../services/feedback.service';
import { RATING_OPTIONS, FEEDBACK_SECTIONS, ALL_RATING_FIELDS } from '../../modules/feedback/feedback-form-config';

/**
 * Mandatory Module Feedback gate — successor of the legacy JSP's forced
 * feedback modal. Lives in MainLayout (outside <router-outlet>) so it can
 * intercept every authenticated route, not just one page.
 *
 * Route exclusion: any route whose deepest ActivatedRoute has
 * `data: { excludeFeedbackCheck: true }` is skipped — this is where a future
 * live exam/test-taking route should opt out, since a student mid-exam must
 * never be interrupted by this modal. No such route exists yet as of writing;
 * once one is added, tag it and the check below picks it up automatically.
 * "checked" stays false while on an excluded route, so the very next
 * navigation away from it re-attempts the check — satisfying "defer until
 * back on a dashboard route" without any extra bookkeeping.
 */
@Component({
  selector: 'app-mandatory-feedback-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './mandatory-feedback-modal.component.html',
  styleUrls: ['./mandatory-feedback-modal.component.css'],
})
export class MandatoryFeedbackModalComponent implements OnInit {
  @ViewChild('feedbackDialog') feedbackDialog!: ElementRef<HTMLDialogElement>;

  ratingOptions = RATING_OPTIONS;
  sections = FEEDBACK_SECTIONS;

  pendingItems: FeedbackPendingItem[] = [];
  currentIndex = 0;

  showSuccess = false;
  submitting = false;
  errorMessage = '';

  formModel: { [key: string]: string } = { ...emptyFeedbackForm() };

  private checked = false;

  constructor(
    private feedbackService: FeedbackService,
    private router: Router,
    private activatedRoute: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.router.events.pipe(filter((e) => e instanceof NavigationEnd)).subscribe(() => this.maybeCheck());
    this.maybeCheck();
  }

  get current(): FeedbackPendingItem | null {
    return this.pendingItems[this.currentIndex] ?? null;
  }

  get remainingCount(): number {
    return this.pendingItems.length - this.currentIndex - 1;
  }

  private maybeCheck(): void {
    if (this.checked || this.isExcludedRoute()) {
      return;
    }
    this.checked = true;

    this.feedbackService.getPending().subscribe({
      next: (items) => {
        const pending = items.filter((i) => !i.alreadySubmitted);
        if (pending.length > 0) {
          this.pendingItems = pending;
          this.currentIndex = 0;
          this.resetForm();
          this.openDialog();
        }
      },
      error: () => {
        // Fail open — say nothing, do nothing, never block the dashboard.
      },
    });
  }

  private isExcludedRoute(): boolean {
    let route = this.activatedRoute.root;
    while (route.firstChild) {
      route = route.firstChild;
    }
    return !!route.snapshot.data?.['excludeFeedbackCheck'];
  }

  private openDialog(): void {
    this.feedbackDialog.nativeElement.showModal();
  }

  /** Non-dismissible: the dialog's native "cancel" event fires on Escape — swallow it. */
  onDialogCancel(event: Event): void {
    event.preventDefault();
  }

  private resetForm(): void {
    this.formModel = { ...emptyFeedbackForm() };
    this.errorMessage = '';
  }

  isRequiredMissing(field: string): boolean {
    return !this.formModel[field];
  }

  onSubmit(): void {
    this.errorMessage = '';
    const item = this.current;
    if (!item) {
      return;
    }

    for (const field of ALL_RATING_FIELDS) {
      if (this.isRequiredMissing(field)) {
        this.errorMessage = 'Please select an option for all criteria.';
        return;
      }
    }

    this.submitting = true;
    this.feedbackService
      .submit({
        batch: item.batch,
        module: item.module,
        trainer: item.trainer,
        form: this.formModel as any,
      })
      .subscribe({
        next: () => {
          this.submitting = false;
          this.showSuccess = true;
        },
        error: (err) => {
          this.submitting = false;
          this.errorMessage = err?.error?.message || 'Something went wrong. Please try again.';
        },
      });
  }

  onContinue(): void {
    if (this.currentIndex >= this.pendingItems.length - 1) {
      // Last module done — reload so the dashboard/session state is fresh and
      // this check runs again from a clean slate (finding nothing pending).
      window.location.reload();
      return;
    }
    this.currentIndex++;
    this.resetForm();
    this.showSuccess = false;
  }
}
