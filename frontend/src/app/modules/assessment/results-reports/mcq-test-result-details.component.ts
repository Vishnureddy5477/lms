import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DashboardService } from '../../../services/dashboard.service';
import { AssessmentService } from '../../../services/assessment.service';

export interface TestResult {
  sNo: number;
  test: string;
  attempt: string;
  question: string;
  answer: string;
  correct_answer: string;
  status: string;
  datetime: string;
}

export interface Summary {
  totalQuestions: number;
  correctAnswers: number;
  incorrectAnswers: number;
  totalAttempts: number;
}

@Component({
  selector: 'app-mcq-test-result-details',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './mcq-test-result-details.component.html',
  styleUrls: ['./mcq-test-result-details.component.css']
})
export class McqTestResultDetailsComponent implements OnInit {

  registrationNo = '';
  selectedModule = '';
  isLoading = false;

  modules: string[] = [];

  summary: Summary = {
    totalQuestions: 0,
    correctAnswers: 0,
    incorrectAnswers: 0,
    totalAttempts: 0
  };

  pageSize = 25;
  searchTerm = '';

  results: TestResult[] = [];

  constructor(private dashboardService: DashboardService, private assessmentService: AssessmentService) {}

  ngOnInit(): void {
    this.dashboardService.getProfile().subscribe({
      next: (profile) => (this.registrationNo = profile.regNo),
    });

    this.assessmentService.getResultModules().subscribe({
      next: (modules) => {
        this.modules = modules;
        if (modules.length > 0) {
          this.selectedModule = modules[0];
          this.loadTestResults();
        }
      },
    });
  }

  loadTestResults(): void {
    if (!this.selectedModule) {
      return;
    }

    this.isLoading = true;

    this.assessmentService.getTestResults(this.selectedModule).subscribe({
      next: (res) => {
        this.results = res.results.map((r, i) => ({
          sNo: i + 1,
          test: r.test,
          attempt: r.attempt,
          question: r.question,
          answer: r.answer,
          correct_answer: r.correctAnswer,
          status: r.status,
          datetime: r.datetime,
        }));
        this.summary = res.summary;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  // Filter results based on search input
  get filteredResults(): TestResult[] {
    if (!this.searchTerm.trim()) {
      return this.results;
    }
    const term = this.searchTerm.toLowerCase();
    return this.results.filter(r =>
      r.test.toLowerCase().includes(term) ||
      r.question.toLowerCase().includes(term) ||
      r.answer.toLowerCase().includes(term) ||
      r.correct_answer.toLowerCase().includes(term)
    );
  }

  // Format date helper matching formatDateTime in original JSP
  formatDateTime(dateTimeString: string): string {
    const date = new Date(dateTimeString);
    if (isNaN(date.getTime())) {
      return dateTimeString;
    }
    return date.toLocaleString('en-IN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

}
