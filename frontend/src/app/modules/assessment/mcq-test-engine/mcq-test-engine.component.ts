import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import {
  AssessmentService, McqSessionState, McqQuestionItem,
  McqSubmitResult, McqAnswerItem,
} from '../../../services/assessment.service';

/** One strike per event cluster — an Alt+Tab fires several events, not three strikes. */
const VIOLATION_DEBOUNCE_MS = 2500;
/** Long enough to coalesce a burst of clicks, short enough that a crash loses nothing. */
const SAVE_DEBOUNCE_MS = 400;
/** Idle keep-alive. Skipped entirely whenever a save has synced recently. */
const HEARTBEAT_INTERVAL_MS = 20000;

@Component({
  selector: 'app-mcq-test-engine',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './mcq-test-engine.component.html',
  styleUrls: ['./mcq-test-engine.component.css'],
})
export class McqTestEngineComponent implements OnInit, OnDestroy {

  state: McqSessionState | null = null;
  loading = true;
  error = '';

  currentIndex = 0;
  timeRemaining = 0;
  savingStatus = '';

  needsFullscreen = false;
  showSubmitModal = false;
  submitted = false;
  submitting = false;
  result: McqSubmitResult | null = null;

  // proctoring
  violationCount = 0;
  maxViolations = 3;
  showViolation = false;
  violationTitle = '';
  violationReason = '';
  violationLevel: 'warn' | 'serious' | 'critical' = 'warn';
  autoSubmitCountdown = 5;

  /** [1..maxViolations] — drives the strike dots in the warning modal. */
  get strikeSlots(): number[] {
    return Array.from({ length: this.maxViolations }, (_, i) => i + 1);
  }

  private saveQueue = new Map<number, number>();
  private saveTimer: ReturnType<typeof setTimeout> | null = null;
  private timerInterval: ReturnType<typeof setInterval> | null = null;
  private heartbeatInterval: ReturnType<typeof setInterval> | null = null;
  private violationDebounceTimer: ReturnType<typeof setTimeout> | null = null;
  private autoSubmitTimer: ReturnType<typeof setInterval> | null = null;
  private armedTimer: ReturnType<typeof setTimeout> | null = null;

  private isViolationArmed = false;
  private lastSyncAt = 0;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private assessmentService: AssessmentService,
  ) {}

  // ─── Lifecycle ────────────────────────────────────────────────────

  ngOnInit(): void {
    const module = this.route.snapshot.queryParamMap.get('module') ?? '';
    const testNo = Number(this.route.snapshot.queryParamMap.get('testNo') ?? 0);

    if (!module || !testNo) {
      this.error = 'Missing test details. Please start the test from your test list.';
      this.loading = false;
      return;
    }

    document.body.classList.add('mcq-assessment-mode');

    // Start and resume are the same call: if a session for this test is still
    // open the server hands it back with the same questions, the saved answers
    // and the clock where it left off.
    this.assessmentService.mcqStartSession(module, testNo).subscribe({
      next: (state) => {
        this.state = state;
        this.timeRemaining = state.remainingSeconds;
        this.violationCount = state.violationCount;
        this.maxViolations = state.maxViolations;
        this.loading = false;

        // Strikes are restored from the server — a refresh cannot wind them back.
        if (this.violationCount >= this.maxViolations) {
          this.doSubmit(true);
          return;
        }
        if (this.timeRemaining <= 0) {
          this.doSubmit(true);
          return;
        }

        this.needsFullscreen = !document.fullscreenElement;
        if (!this.needsFullscreen) {
          this.armViolations();
        }
        this.startTimer();
        this.startHeartbeat();
      },
      error: (err) => {
        this.error = err?.error?.message || 'Could not start this test. Please return to your test list and try again.';
        this.loading = false;
      },
    });
  }

  ngOnDestroy(): void {
    document.body.classList.remove('mcq-assessment-mode');
    this.clearTimers();
    this.isViolationArmed = false;
  }

  // ─── Questions & navigation ───────────────────────────────────────

  get currentQuestion(): McqQuestionItem | undefined {
    return this.state?.questions?.[this.currentIndex];
  }

  /** Question text may carry a code snippet on later lines — split so it renders monospaced. */
  get currentParts(): { prompt: string; code: string } | null {
    const t = this.currentQuestion?.question;
    if (!t || !t.includes('\n')) return null;
    const idx = t.indexOf('\n');
    return { prompt: t.slice(0, idx).trim(), code: t.slice(idx).trim() };
  }

  optionsOf(q: McqQuestionItem): { value: number; text: string }[] {
    return [
      { value: 1, text: q.opt1 },
      { value: 2, text: q.opt2 },
      { value: 3, text: q.opt3 },
      { value: 4, text: q.opt4 },
    ];
  }

  selectedFor(order: number): number {
    return this.state?.answers?.[order] ?? 0;
  }

  isAnswered(order: number): boolean {
    return this.selectedFor(order) > 0;
  }

  goTo(index: number): void {
    if (!this.state?.questions) return;
    if (index < 0 || index >= this.state.questions.length) return;
    this.currentIndex = index;
  }

  next(): void { this.goTo(this.currentIndex + 1); }
  prev(): void { this.goTo(this.currentIndex - 1); }

  get isFirst(): boolean { return this.currentIndex === 0; }
  get isLast(): boolean {
    return this.currentIndex === (this.state?.questions?.length ?? 1) - 1;
  }

  get answeredCount(): number {
    if (!this.state?.answers) return 0;
    return Object.values(this.state.answers).filter((v) => v > 0).length;
  }

  get progressPercent(): number {
    const total = this.state?.questions?.length ?? 0;
    return total === 0 ? 0 : (this.answeredCount / total) * 100;
  }

  get attemptedMarks(): number {
    return this.answeredCount * 0.5;
  }

  // ─── Answering ────────────────────────────────────────────────────

  selectOption(order: number, option: number): void {
    if (!this.state) return;
    if (!this.state.answers) this.state.answers = {};
    this.state.answers[order] = option;
    this.enqueueSave(order, option);
  }

  clearResponse(order: number): void {
    if (!this.state?.answers) return;
    this.state.answers[order] = 0;
    this.enqueueSave(order, 0);
  }

  /**
   * Queue an answer and flush shortly after.
   *
   * The legacy panel fired one request per click and blocked Next until it
   * acked. Coalescing here keeps the same crash-safety — at most one click is
   * ever unsaved, for at most 400ms — without ever blocking navigation.
   */
  private enqueueSave(order: number, option: number): void {
    this.saveQueue.set(order, option);
    if (this.saveTimer) clearTimeout(this.saveTimer);
    this.saveTimer = setTimeout(() => this.flushSave(), SAVE_DEBOUNCE_MS);
  }

  private flushSave(retries = 3): void {
    if (!this.state?.sessionId || this.saveQueue.size === 0) return;

    const answers: McqAnswerItem[] = Array.from(this.saveQueue.entries())
      .map(([questionOrder, selectedOption]) => ({ questionOrder, selectedOption }));
    this.saveQueue.clear();
    this.savingStatus = 'Saving…';

    this.assessmentService.mcqSaveAnswers(this.state.sessionId, answers).subscribe({
      next: (sync) => {
        this.savingStatus = '';
        this.applySync(sync);
      },
      error: () => {
        if (retries > 0) {
          // Put them back and try again — never silently drop an answer.
          answers.forEach((a) => {
            if (!this.saveQueue.has(a.questionOrder)) {
              this.saveQueue.set(a.questionOrder, a.selectedOption);
            }
          });
          this.savingStatus = 'Offline — retrying…';
          setTimeout(() => this.flushSave(retries - 1), 2000);
        } else {
          this.savingStatus = 'Save failed';
        }
      },
    });
  }

  // ─── Server-authoritative clock & strikes ─────────────────────────

  /** The server's word on the clock and the strike count always wins. */
  private applySync(sync: { remainingSeconds: number; violationCount: number; maxViolations: number; shouldAutoSubmit: boolean; reason: string | null }): void {
    this.lastSyncAt = Date.now();
    this.timeRemaining = sync.remainingSeconds;
    this.violationCount = sync.violationCount;
    this.maxViolations = sync.maxViolations;

    if (sync.shouldAutoSubmit && !this.submitted && !this.submitting) {
      this.doSubmit(true);
    }
  }

  private startTimer(): void {
    this.timerInterval = setInterval(() => {
      if (this.timeRemaining > 0) {
        this.timeRemaining--;
      } else if (!this.submitted && !this.submitting) {
        this.doSubmit(true);
      }
    }, 1000);
  }

  /** Only fires when a save has not already synced us recently. */
  private startHeartbeat(): void {
    this.heartbeatInterval = setInterval(() => {
      if (!this.state?.sessionId || this.submitted || this.submitting) return;
      if (Date.now() - this.lastSyncAt < HEARTBEAT_INTERVAL_MS) return;

      this.assessmentService.mcqHeartbeat(this.state.sessionId).subscribe({
        next: (sync) => this.applySync(sync),
        error: () => { /* keep the local countdown; a blip must never shorten the clock */ },
      });
    }, HEARTBEAT_INTERVAL_MS);
  }

  get timerDisplay(): string {
    const m = Math.floor(this.timeRemaining / 60).toString().padStart(2, '0');
    const s = (this.timeRemaining % 60).toString().padStart(2, '0');
    return `${m}:${s}`;
  }

  get timerDanger(): boolean { return this.timeRemaining < 300; }
  get timerCritical(): boolean { return this.timeRemaining < 120; }

  // ─── Proctoring ───────────────────────────────────────────────────

  enterFullscreen(): void {
    this.requestFullscreen();
    this.needsFullscreen = false;
    this.armViolations();
  }

  private armViolations(): void {
    // Short grace period so entering fullscreen doesn't trip its own listener.
    this.armedTimer = setTimeout(() => { this.isViolationArmed = true; }, 600);
  }

  private requestFullscreen(): void {
    const el = document.documentElement as any;
    (el.requestFullscreen || el.webkitRequestFullscreen)?.call(el)?.catch?.(() => {});
  }

  @HostListener('document:visibilitychange')
  onVisibilityChange(): void {
    if (document.visibilityState === 'hidden') {
      this.triggerViolation('tab-switch', 'Tab switch or window minimise detected');
    }
  }

  @HostListener('window:blur')
  onWindowBlur(): void {
    this.triggerViolation('window-blur', 'Window lost focus (Alt+Tab or similar)');
  }

  @HostListener('document:fullscreenchange')
  onFullscreenChange(): void {
    if (!document.fullscreenElement && !this.submitted && !this.submitting) {
      this.triggerViolation('fullscreen-exit', 'Exited fullscreen mode');
    }
  }

  @HostListener('document:keydown', ['$event'])
  onKeydown(e: KeyboardEvent): void {
    const key = (e.key || '').toUpperCase();
    const isDevtools =
      e.key === 'F12' ||
      (e.ctrlKey && e.shiftKey && ['I', 'J', 'C', 'K'].includes(key)) ||
      (e.ctrlKey && key === 'U');

    if (isDevtools) {
      e.preventDefault();
      this.triggerViolation('devtools', 'Developer tools shortcut detected');
    }
  }

  @HostListener('document:contextmenu', ['$event'])
  onContextMenu(e: MouseEvent): void { e.preventDefault(); }

  @HostListener('document:copy', ['$event'])
  onCopy(e: ClipboardEvent): void { e.preventDefault(); }

  @HostListener('document:cut', ['$event'])
  onCut(e: ClipboardEvent): void { e.preventDefault(); }

  /**
   * Report a strike.
   *
   * The count shown here is only ever the SERVER's, applied through
   * {@link applySync} — this method never decides the total itself.
   */
  private triggerViolation(type: string, reason: string): void {
    if (!this.isViolationArmed || this.submitted || this.submitting) return;
    if (this.violationDebounceTimer) return;

    this.violationDebounceTimer = setTimeout(() => {
      this.violationDebounceTimer = null;
    }, VIOLATION_DEBOUNCE_MS);

    if (!this.state?.sessionId) return;

    this.assessmentService.mcqReportViolation(this.state.sessionId, type, reason).subscribe({
      next: (sync) => {
        this.violationCount = sync.violationCount;
        this.maxViolations = sync.maxViolations;
        this.lastSyncAt = Date.now();
        this.showStrike(reason, sync.violationCount, sync.maxViolations);

        if (sync.shouldAutoSubmit) {
          this.startAutoSubmitCountdown();
        }
      },
      error: () => {
        // Show the warning even if the report failed to reach the server; the
        // count reconciles on the next sync.
        this.showStrike(reason, this.violationCount + 1, this.maxViolations);
      },
    });
  }

  private showStrike(reason: string, count: number, max: number): void {
    this.violationReason = reason;
    this.showViolation = true;

    if (count >= max) {
      this.violationLevel = 'critical';
      this.violationTitle = 'Final warning — submitting your test';
    } else if (count === max - 1) {
      this.violationLevel = 'serious';
      this.violationTitle = `Warning ${count} of ${max} — one more will end your test`;
    } else {
      this.violationLevel = 'warn';
      this.violationTitle = `Warning ${count} of ${max}`;
    }
  }

  private startAutoSubmitCountdown(): void {
    this.autoSubmitCountdown = 5;
    this.autoSubmitTimer = setInterval(() => {
      this.autoSubmitCountdown--;
      if (this.autoSubmitCountdown <= 0) {
        if (this.autoSubmitTimer) { clearInterval(this.autoSubmitTimer); this.autoSubmitTimer = null; }
        this.showViolation = false;
        this.doSubmit(true);
      }
    }, 1000);
  }

  dismissViolation(): void {
    if (this.violationLevel === 'critical') return;
    this.showViolation = false;
    this.requestFullscreen();
  }

  // ─── Submission ───────────────────────────────────────────────────

  openSubmitModal(): void { this.showSubmitModal = true; }

  cancelSubmit(): void {
    this.showSubmitModal = false;
    this.requestFullscreen();
  }

  confirmSubmit(): void {
    this.showSubmitModal = false;
    this.doSubmit(false);
  }

  private doSubmit(auto: boolean): void {
    if (this.submitted || this.submitting) return;
    this.submitting = true;

    this.clearTimers();
    this.isViolationArmed = false;

    const sessionId = this.state?.sessionId;
    if (!sessionId) {
      this.submitting = false;
      this.error = 'This test session has been lost. Please contact your administrator.';
      return;
    }

    const send = () => {
      this.assessmentService.mcqSubmit(sessionId, auto).subscribe({
        next: (result) => {
          this.result = result;
          this.submitted = true;
          this.submitting = false;
          this.exitFullscreen();
        },
        error: (err) => {
          this.submitting = false;
          this.error = err?.error?.message || 'Could not submit your test. Please try again.';
          this.exitFullscreen();
        },
      });
    };

    // Flush anything still queued so a last-second answer is never lost.
    if (this.saveQueue.size > 0 && this.state) {
      const answers: McqAnswerItem[] = Array.from(this.saveQueue.entries())
        .map(([questionOrder, selectedOption]) => ({ questionOrder, selectedOption }));
      this.saveQueue.clear();
      this.assessmentService.mcqSaveAnswers(this.state.sessionId, answers).subscribe({
        next: () => send(),
        error: () => send(),   // submit regardless — the score must still be recorded
      });
    } else {
      send();
    }
  }

  backToList(): void {
    this.router.navigate(['/assessment/quizzes-tests']);
  }

  // ─── Cleanup ──────────────────────────────────────────────────────

  private exitFullscreen(): void {
    if (document.fullscreenElement) {
      document.exitFullscreen?.().catch(() => {});
    }
  }

  private clearTimers(): void {
    if (this.timerInterval) { clearInterval(this.timerInterval); this.timerInterval = null; }
    if (this.heartbeatInterval) { clearInterval(this.heartbeatInterval); this.heartbeatInterval = null; }
    if (this.saveTimer) { clearTimeout(this.saveTimer); this.saveTimer = null; }
    if (this.autoSubmitTimer) { clearInterval(this.autoSubmitTimer); this.autoSubmitTimer = null; }
    if (this.violationDebounceTimer) { clearTimeout(this.violationDebounceTimer); this.violationDebounceTimer = null; }
    if (this.armedTimer) { clearTimeout(this.armedTimer); this.armedTimer = null; }
  }
}
