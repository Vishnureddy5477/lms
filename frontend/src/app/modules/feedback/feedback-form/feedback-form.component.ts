import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { FeedbackService, emptyFeedbackForm } from '../../../services/feedback.service';
import { RATING_OPTIONS, FEEDBACK_SECTIONS as SECTIONS } from '../feedback-form-config';
import { SkeletonComponent } from '../../../shared/skeleton/skeleton.component';

@Component({
  selector: 'app-feedback-form',
  standalone: true,
  imports: [CommonModule, FormsModule, SkeletonComponent],
  templateUrl: './feedback-form.component.html',
  styleUrls: ['./feedback-form.component.css']
})
export class FeedbackFormComponent implements OnInit {

  ratingOptions = RATING_OPTIONS;
  sections = SECTIONS;

  batch = '';
  module = '';
  trainer = '';
  mode: 'give' | 'edit' | 'view' = 'give';

  formModel: { [key: string]: string } = { ...emptyFeedbackForm() };

  loading = true;
  submitting = false;
  errorMessage = '';

  get readonly(): boolean {
    return this.mode === 'view';
  }

  get pageTitle(): string {
    if (this.mode === 'view') return 'Your Submitted Feedback';
    if (this.mode === 'edit') return 'Edit Feedback';
    return 'Student Feedback Form';
  }

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private feedbackService: FeedbackService
  ) {}

  ngOnInit(): void {
    const params = this.route.snapshot.queryParamMap;
    this.batch = params.get('batch') || '';
    this.module = params.get('module') || '';
    this.trainer = params.get('trainer') || '';
    this.mode = (params.get('mode') as 'give' | 'edit' | 'view') || 'give';

    if (this.mode === 'give') {
      this.loading = false;
      return;
    }

    this.feedbackService.getForm(this.batch, this.module, this.trainer).subscribe({
      next: (data) => {
        if (data) {
          this.formModel = { ...data } as unknown as { [key: string]: string };
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  isRequiredMissing(field: string): boolean {
    return !this.formModel[field];
  }

  onSubmit(): void {
    this.errorMessage = '';

    for (const section of this.sections) {
      for (const row of section.rows) {
        if (this.isRequiredMissing(row.field)) {
          this.errorMessage = 'Please select an option for all criteria.';
          return;
        }
      }
    }

    this.submitting = true;
    this.feedbackService
      .submit({
        batch: this.batch,
        module: this.module,
        trainer: this.trainer,
        form: this.formModel as any,
      })
      .subscribe({
        next: (res) => {
          this.submitting = false;
          alert(res.message);
          this.router.navigate(['/feedback/submit-feedback']);
        },
        error: (err) => {
          this.submitting = false;
          this.errorMessage = err?.error?.message || 'Something went wrong. Try again or connect with CRM department.';
        },
      });
  }

  goBack(): void {
    this.router.navigate([this.mode === 'view' ? '/feedback/feedback-history' : '/feedback/submit-feedback']);
  }

}
