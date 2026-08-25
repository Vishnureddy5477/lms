import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AssessmentService } from '../../../services/assessment.service';
import { SkeletonComponent } from '../../../shared/skeleton/skeleton.component';

export interface McqTestItem {
  id: string;
  moduleName: string;
  batchName: string;
  mcqTestNo: number;
  testDate: string;
  testStartTime: string;
  testEndTime: string;
  testType: string; // 'scheduled' | 'retake'
}

type TestWindow = 'upcoming' | 'live' | 'ended';

@Component({
  selector: 'app-quizzes-tests',
  standalone: true,
  imports: [CommonModule, SkeletonComponent],
  templateUrl: './quizzes-tests.component.html',
  styleUrls: ['./quizzes-tests.component.css']
})
export class QuizzesTestsComponent implements OnInit {

  availableTests: McqTestItem[] = [];
  isLoading = false;

  /** Which card is mid access-check, so only that button shows a spinner. */
  validatingId: string | null = null;

  /** The reason the last start attempt was refused, shown as a dismissible alert. */
  blockedTitle = '';
  blockedMessage = '';

  /**
   * The test that passed the gate and is waiting on the student to accept the
   * rules. Non-null means the rules popup is open.
   */
  pendingTest: McqTestItem | null = null;

  constructor(
    private assessmentService: AssessmentService,
    private router: Router,
  ) { }

  ngOnInit(): void {
    this.onRefreshTests();
  }

  onRefreshTests(): void {
    this.isLoading = true;
    this.dismissBlocked();
    this.assessmentService.getAvailableTests().subscribe({
      next: (tests) => {
        this.availableTests = tests.map((t) => ({
          id: t.id,
          moduleName: t.moduleName,
          batchName: t.batchName,
          mcqTestNo: Number(t.mcqTestNo),
          testDate: t.testDate,
          testStartTime: t.testStartTime,
          testEndTime: t.testEndTime,
          testType: t.testType,
        }));
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  // ─── Time window (display only) ──────────────────────────────────
  // The server re-checks the window authoritatively in the access gate; this
  // is purely so the card can show Live / Upcoming / Ended and a countdown.

  windowOf(test: McqTestItem): TestWindow {
    const now = new Date();
    const start = this.at(test.testDate, test.testStartTime);
    const end = this.at(test.testDate, test.testEndTime);
    if (!start || !end) return 'upcoming';
    if (now < start) return 'upcoming';
    if (now > end) return 'ended';
    return 'live';
  }

  isLive(test: McqTestItem): boolean { return this.windowOf(test) === 'live'; }

  /** "1h 19m remaining" while live, "Starts in 2h 5m" before. */
  countdownOf(test: McqTestItem): string {
    const now = new Date();
    const start = this.at(test.testDate, test.testStartTime);
    const end = this.at(test.testDate, test.testEndTime);
    if (!start || !end) return '';

    const w = this.windowOf(test);
    if (w === 'ended') return 'This test has ended';

    const target = w === 'live' ? end : start;
    const diffMs = target.getTime() - now.getTime();
    const hours = Math.floor(diffMs / 3600000);
    const minutes = Math.floor((diffMs % 3600000) / 60000);
    const span = `${hours}h ${minutes}m`;
    return w === 'live' ? `${span} remaining` : `Starts in ${span}`;
  }

  private at(date: string, time: string): Date | null {
    if (!date) return null;
    const t = (time && time.trim()) ? time.trim() : '00:00:00';
    const parsed = new Date(`${date.trim()}T${t.length === 5 ? t + ':00' : t}`);
    return isNaN(parsed.getTime()) ? null : parsed;
  }

  // ─── Starting a test ─────────────────────────────────────────────

  /**
   * Run the server-side access gate, then hand off to the test engine.
   *
   * Every blocking reason (too early, too late, already passed, payment
   * required, incomplete question bank) comes back from the server with its own
   * message, so the student is told what actually stopped them rather than a
   * single generic refusal.
   */
  onStartTest(test: McqTestItem): void {
    this.dismissBlocked();
    this.validatingId = test.id;

    this.assessmentService.mcqAccessCheck(test.moduleName, test.mcqTestNo).subscribe({
      next: (result) => {
        this.validatingId = null;
        if (!result.allowed) {
          this.blockedTitle = result.title;
          this.blockedMessage = result.message;
          return;
        }
        // Gate passed — show the rules here rather than navigating to a
        // separate screen to say the same thing.
        this.pendingTest = test;
      },
      error: (err) => {
        this.validatingId = null;
        this.blockedTitle = 'Could not start the test';
        this.blockedMessage = err?.error?.message
          || 'We could not verify your access just now. Please check your connection and try again.';
      },
    });
  }

  /**
   * Accept the rules and go.
   *
   * Fullscreen is requested HERE, straight out of the click, because browsers
   * only grant it during a user gesture. The route change that follows is
   * in-document, so fullscreen carries into the test engine and it starts
   * armed — no second prompt.
   */
  beginTest(): void {
    const test = this.pendingTest;
    if (!test) return;
    this.pendingTest = null;

    const el = document.documentElement as any;
    const request = el.requestFullscreen || el.webkitRequestFullscreen;

    const go = () => this.router.navigate(['/assessment/mcq-test/attempt'], {
      queryParams: { module: test.moduleName, testNo: test.mcqTestNo },
    });

    if (request) {
      // Navigate whether or not fullscreen is granted — the engine falls back
      // to its own prompt if the browser refused.
      Promise.resolve(request.call(el)).then(go, go);
    } else {
      go();
    }
  }

  cancelStart(): void {
    this.pendingTest = null;
  }

  dismissBlocked(): void {
    this.blockedTitle = '';
    this.blockedMessage = '';
  }
}
