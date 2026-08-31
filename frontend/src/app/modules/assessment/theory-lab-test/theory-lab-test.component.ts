import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  AssessmentService,
  TheoryQuestionPaper,
  TheoryTestItem,
} from '../../../services/assessment.service';
import { AuthService } from '../../../services/auth.service';
import { SkeletonComponent } from '../../../shared/skeleton/skeleton.component';
import { SecurePdfViewerComponent } from '../../../shared/secure-pdf-viewer/secure-pdf-viewer.component';

type TestStatus = 'upcoming' | 'live' | 'ended';

/**
 * One card on the schedule.
 *
 * `secondsUntilStart` / `secondsRemaining` start as the server's numbers and
 * are ticked down locally once a second. The local tick is cosmetic: it keeps
 * the countdown smooth between refreshes, but every decision that matters
 * (handing out the paper, streaming it) is re-checked server-side.
 */
interface ScheduledTest {
  id: string;
  moduleName: string;
  domainName: string;
  isLab: boolean;
  batchName: string;
  testDate: string;
  startTime: string;
  endTime: string;
  totalMarks: string;
  totalQuestions: string;
  status: TestStatus;
  secondsUntilStart: number;
  secondsRemaining: number;
  durationSeconds: number;
}

/** The paper currently on screen. Non-null means the viewer modal is open. */
interface OpenPaper {
  test: ScheduledTest;
  fileUrl: string;
  questionPaperId: string;
}

@Component({
  selector: 'app-theory-lab-test',
  standalone: true,
  imports: [CommonModule, SkeletonComponent, SecurePdfViewerComponent],
  templateUrl: './theory-lab-test.component.html',
  styleUrls: ['./theory-lab-test.component.css'],
})
export class TheoryLabTestComponent implements OnInit, OnDestroy {

  scheduledTests: ScheduledTest[] = [];
  isLoading = false;

  /** Which card is waiting on the server, so only that button spins. */
  openingId: string | null = null;

  /** Why the last attempt to open a paper was refused. */
  blockedTitle = '';
  blockedMessage = '';

  openPaper: OpenPaper | null = null;

  /** Set when a test ends with its paper still open — replaces the viewer. */
  timeUpFor: string | null = null;

  /** Stamped across the paper so a photograph carries who took it. */
  watermark = '';

  private ticker: ReturnType<typeof setInterval> | null = null;

  constructor(
    private assessmentService: AssessmentService,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    const student = this.authService.getStudent();
    this.watermark = [student?.name, student?.regNo].filter(Boolean).join(' · ');
    this.load();
    this.ticker = setInterval(() => this.tick(), 1000);
  }

  ngOnDestroy(): void {
    if (this.ticker) clearInterval(this.ticker);
  }

  load(): void {
    this.isLoading = true;
    this.dismissBlocked();
    this.assessmentService.getTheoryTests().subscribe({
      next: (tests) => {
        this.scheduledTests = tests.map((t) => this.toCard(t));
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  private toCard(t: TheoryTestItem): ScheduledTest {
    return {
      id: t.theoryTestId,
      moduleName: t.moduleName,
      domainName: t.domainName,
      isLab: (t.testType || '').toLowerCase() === 'lab',
      batchName: t.batchName,
      testDate: t.testDate,
      startTime: t.testStartTime,
      endTime: t.testEndTime,
      totalMarks: t.totalMarks,
      totalQuestions: t.totalQuestions,
      status: t.status,
      secondsUntilStart: t.secondsUntilStart,
      secondsRemaining: t.secondsRemaining,
      durationSeconds: t.durationSeconds,
    };
  }

  // ─── The clock ───────────────────────────────────────────────────

  /**
   * Walk the countdowns forward one second.
   *
   * Crossing a boundary here only changes what the card shows; the server is
   * still the one that decides whether a paper may be opened. The one thing
   * this does enforce is closing a paper that is on screen when time runs out.
   */
  private tick(): void {
    for (const test of this.scheduledTests) {
      if (test.status === 'upcoming') {
        test.secondsUntilStart = Math.max(test.secondsUntilStart - 1, 0);
        test.secondsRemaining = Math.max(test.secondsRemaining - 1, 0);
        if (test.secondsUntilStart === 0) {
          test.status = 'live';
        }
      } else if (test.status === 'live') {
        test.secondsRemaining = Math.max(test.secondsRemaining - 1, 0);
        if (test.secondsRemaining === 0) {
          test.status = 'ended';
          if (this.openPaper?.test.id === test.id) {
            // Time is up with the paper still on screen: take it away and say why.
            this.timeUpFor = test.moduleName;
            this.openPaper = null;
          }
        }
      }
    }
  }

  /** How far through the window we are, for the progress bar on a live card. */
  progressOf(test: ScheduledTest): number {
    if (test.status === 'ended') return 100;
    if (test.status === 'upcoming' || !test.durationSeconds) return 0;
    const elapsed = test.durationSeconds - test.secondsRemaining;
    return Math.min(Math.max((elapsed / test.durationSeconds) * 100, 0), 100);
  }

  /** "02:48:37" — the shape the legacy card used. */
  clockOf(test: ScheduledTest): string {
    const seconds = test.status === 'upcoming' ? test.secondsUntilStart : test.secondsRemaining;
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    const s = seconds % 60;
    return [h, m, s].map((n) => String(n).padStart(2, '0')).join(':');
  }

  // ─── Opening the paper ───────────────────────────────────────────

  /**
   * Ask the server for this student's paper.
   *
   * The card only offers this while the test reads as live, but that is a
   * courtesy — the server runs the same check again and refuses on its own
   * terms, which is what actually closes the hole the legacy servlet had.
   */
  viewQuestions(test: ScheduledTest): void {
    this.dismissBlocked();
    this.timeUpFor = null;
    this.openingId = test.id;

    this.assessmentService.getTheoryQuestionPaper(test.id).subscribe({
      next: (paper: TheoryQuestionPaper) => {
        this.openingId = null;

        if (!paper.allowed || !paper.fileUrl) {
          this.blockedTitle = paper.title || 'Question Paper Unavailable';
          this.blockedMessage = paper.message || 'This question paper cannot be opened right now.';
          // The refusal is the server's current view of the window; re-sync so
          // the card stops disagreeing with it.
          this.load();
          return;
        }

        // Trust the server's remaining time over the locally ticked one.
        test.secondsRemaining = paper.secondsRemaining;
        test.status = 'live';

        this.openPaper = {
          test,
          fileUrl: paper.fileUrl,
          questionPaperId: paper.questionPaperId || '',
        };
      },
      error: (err) => {
        this.openingId = null;
        this.blockedTitle = 'Could not open the question paper';
        this.blockedMessage = err?.error?.message
          || 'We could not reach the server just now. Please check your connection and try again.';
      },
    });
  }

  closePaper(): void {
    this.openPaper = null;
  }

  dismissTimeUp(): void {
    this.timeUpFor = null;
  }

  dismissBlocked(): void {
    this.blockedTitle = '';
    this.blockedMessage = '';
  }

  onViewerFailed(message: string): void {
    this.openPaper = null;
    this.blockedTitle = 'Could not display the question paper';
    this.blockedMessage = message;
  }
}
