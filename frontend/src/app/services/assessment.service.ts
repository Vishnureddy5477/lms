import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ProjectItem {
  type: string;
  title: string;
  link: string;
  module: string;
  totalMarks: string;
  obtainedMarks: string;
  remarks: string;
  date: string;
}

export interface TestResultRow {
  examId: string;
  subject: string;
  noOfTest: string;
  test: string;
  question: string;
  answer: string;
  answerOption: string;
  correctAnswer: string;
  status: string;
  attempt: string;
  datetime: string;
}

export interface TestResultsSummary {
  totalQuestions: number;
  correctAnswers: number;
  incorrectAnswers: number;
  totalAttempts: number;
}

export interface TestResultsResponse {
  results: TestResultRow[];
  summary: TestResultsSummary;
}

export interface AvailableTestItem {
  id: string;
  moduleName: string;
  batchName: string;
  mcqTestNo: string;
  testDate: string;
  testStartTime: string;
  testEndTime: string;
  testType: string;
}

export interface TheoryTestItem {
  theoryTestId: string;
  domainName: string;
  /** 'theory' | 'lab' — the only thing that differs between the two. */
  testType: string;
  moduleName: string;
  batchName: string;
  testDate: string;
  testStartTime: string;
  testEndTime: string;
  totalMarks: string;
  totalQuestions: string;
  /** Bucketed on the SERVER clock, so a wrong device clock cannot fake a window. */
  status: 'upcoming' | 'live' | 'ended';
  secondsUntilStart: number;
  secondsRemaining: number;
  durationSeconds: number;
}

/**
 * The reply to "View Questions".
 *
 * A refusal arrives as allowed=false with a reason rather than an HTTP error,
 * the same shape the MCQ access gate uses.
 */
export interface TheoryQuestionPaper {
  allowed: boolean;
  title: string | null;
  message: string | null;
  theoryTestId: string | null;
  testType: string | null;
  moduleName: string | null;
  /** Which variant of the paper this student is locked to. */
  questionPaperId: string | null;
  /** API path that streams the PDF — never the real storage URL. */
  fileUrl: string | null;
  secondsRemaining: number;
}

// ─── MCQ Module Test engine ────────────────────────────────────────

export interface McqAccessResult {
  allowed: boolean;
  title: string;
  message: string;
}

/** One question slot. There is deliberately no correct-answer field. */
export interface McqQuestionItem {
  questionOrder: number;
  questionId: number;
  question: string;
  opt1: string;
  opt2: string;
  opt3: string;
  opt4: string;
}

export interface McqSessionState {
  sessionId: number;
  moduleName: string;
  testNo: number;
  attemptNo: number;
  studentName: string;
  regNo: string;
  batch: string;
  totalQuestions: number;
  totalMarks: number;
  remainingSeconds: number;
  violationCount: number;
  maxViolations: number;
  questions: McqQuestionItem[];
  /** questionOrder -> selected option (0 = not attempted, 1..4) */
  answers: Record<number, number>;
}

/** {@code selectedOption}: 0 = cleared / not attempted, 1..4 = chosen option. */
export interface McqAnswerItem {
  questionOrder: number;
  selectedOption: number;
}

export interface McqSyncResponse {
  remainingSeconds: number;
  violationCount: number;
  maxViolations: number;
  shouldAutoSubmit: boolean;
  reason: string | null;
}

export interface McqSubmitResult {
  status: string;
  correct: number;
  incorrect: number;
  notAttempted: number;
  obtainedMarks: number;
  totalMarks: number;
  percentage: string;
  attemptNo: number;
  nextDate: string;
  paidSlotRequired: boolean;
  autoSubmitted: boolean;
  moduleName: string;
  testNo: number;
  studentName: string;
  regNo: string;
  batch: string;
}

@Injectable({ providedIn: 'root' })
export class AssessmentService {
  constructor(private http: HttpClient) {}

  // Projects
  listProjects(): Observable<ProjectItem[]> {
    return this.http.get<ProjectItem[]>(`${environment.apiUrl}/student/projects`);
  }

  uploadProject(projectTypeCode: string, projectTitle: string, file: File): Observable<{ success: boolean; message: string }> {
    const formData = new FormData();
    formData.append('projectTypeCode', projectTypeCode);
    formData.append('projectTitle', projectTitle);
    formData.append('file', file);
    return this.http.post<{ success: boolean; message: string }>(`${environment.apiUrl}/student/projects`, formData);
  }

  // Test results
  getResultModules(): Observable<string[]> {
    return this.http.get<string[]>(`${environment.apiUrl}/student/test-results/modules`);
  }

  getTestResults(subject: string): Observable<TestResultsResponse> {
    return this.http.get<TestResultsResponse>(`${environment.apiUrl}/student/test-results`, { params: { subject } });
  }

  // Available tests
  getAvailableTests(): Observable<AvailableTestItem[]> {
    return this.http.get<AvailableTestItem[]>(`${environment.apiUrl}/student/assessment/available-tests`);
  }

  getTheoryTests(): Observable<TheoryTestItem[]> {
    return this.http.get<TheoryTestItem[]>(`${environment.apiUrl}/student/assessment/theory-tests`);
  }

  /**
   * Draw this student's variant of the question paper, or get back the one they
   * were already locked to. Idempotent by design — reopening never re-rolls.
   */
  getTheoryQuestionPaper(theoryTestId: string): Observable<TheoryQuestionPaper> {
    return this.http.post<TheoryQuestionPaper>(
      `${environment.apiUrl}/student/assessment/theory-tests/${theoryTestId}/question-paper`, {});
  }

  // ─── MCQ Module Test engine ──────────────────────────────────────
  // Note what is NOT sent on any of these: registration number, batch,
  // test date/times, or anything about correctness. All of it is taken
  // from the JWT principal and the schedule row on the server.

  /** Run the access gate without starting anything, so the card can show the real reason. */
  mcqAccessCheck(module: string, testNo: number): Observable<McqAccessResult> {
    return this.http.post<McqAccessResult>(
      `${environment.apiUrl}/student/assessment/mcq-test/access-check`, { module, testNo });
  }

  /** Start the attempt, or resume one already open for this test. */
  mcqStartSession(module: string, testNo: number): Observable<McqSessionState> {
    return this.http.post<McqSessionState>(
      `${environment.apiUrl}/student/assessment/mcq-test/session/start`, { module, testNo });
  }

  mcqGetSession(sessionId: number): Observable<McqSessionState> {
    return this.http.get<McqSessionState>(
      `${environment.apiUrl}/student/assessment/mcq-test/session/${sessionId}`);
  }

  /** Debounced batch of answers. The reply carries the authoritative clock and strike count. */
  mcqSaveAnswers(sessionId: number, answers: McqAnswerItem[]): Observable<McqSyncResponse> {
    return this.http.post<McqSyncResponse>(
      `${environment.apiUrl}/student/assessment/mcq-test/session/${sessionId}/answers`, { answers });
  }

  mcqReportViolation(sessionId: number, type: string, reason: string): Observable<McqSyncResponse> {
    return this.http.post<McqSyncResponse>(
      `${environment.apiUrl}/student/assessment/mcq-test/session/${sessionId}/violation`, { type, reason });
  }

  mcqHeartbeat(sessionId: number): Observable<McqSyncResponse> {
    return this.http.get<McqSyncResponse>(
      `${environment.apiUrl}/student/assessment/mcq-test/session/${sessionId}/heartbeat`);
  }

  mcqSubmit(sessionId: number, auto: boolean): Observable<McqSubmitResult> {
    return this.http.post<McqSubmitResult>(
      `${environment.apiUrl}/student/assessment/mcq-test/session/${sessionId}/submit?auto=${auto}`, {});
  }
}
