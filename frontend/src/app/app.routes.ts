import { Routes } from '@angular/router';
import { MainLayout } from './layout/main-layout/main-layout';
import { Login } from './modules/auth/login/login';
import { authGuard } from './guards/auth.guard';
import { OverviewComponent } from './modules/dashboard/overview/overview.component';
import { PersonalInfoComponent } from './modules/profile/personal-info/personal-info.component';
import { BillingPaymentsComponent } from './modules/profile/billing-payments/billing-payments.component';
import { EvaluationPolicyComponent } from './modules/criteria/evaluation-policy/evaluation-policy.component';
import { ProgressComponent } from './modules/progress/progress.component';
import { ClassScheduleComponent } from './modules/schedule/class-schedule/class-schedule.component';
import { DailyAttendanceComponent } from './modules/attendance/daily-attendance/daily-attendance.component';
import { StudyMaterialsComponent } from './modules/resources/study-materials/study-materials.component';
import { ClassNotesComponent } from './modules/resources/class-notes/class-notes.component';
import { ReferenceVideosComponent } from './modules/resources/reference-videos/reference-videos.component';
import { DigitalContentComponent } from './modules/resources/digital-content/digital-content.component';
import { LiveSessionsArchiveComponent } from './modules/tutorials/live-sessions-archive/live-sessions-archive.component';
import { McqPracticeTestsComponent } from '././modules/practice/mcq-practice-tests/mcq-practice-tests.component';
import { McqPracticeResultsComponent } from '././modules/practice/mcq-practice-results/mcq-practice-results.component';
import { ProgrammingExercisesComponent } from './modules/practice/programming-exercises/programming-exercises.component';
import { CodingPracticeComponent } from './modules/practice/coding-practice/coding-practice.component';
import { IndustryInterviewQuestionsComponent } from './modules/practice/industry-interview-questions/industry-interview-questions.component';
import { ProgrammingCompilerComponent } from './modules/practice/programming-compiler/programming-compiler.component';
import { QuizzesTestsComponent } from '././modules/assessment/quizzes-tests/quizzes-tests.component';
import { TheoryLabTestComponent } from './modules/assessment/theory-lab-test/theory-lab-test.component';
import { UploadProjectsComponent } from './modules/assessment/upload-projects/upload-projects.component';
import { McqTestResultDetailsComponent } from './modules/assessment/results-reports/mcq-test-result-details.component';
import { ReportCardComponent } from './modules/performance/report-card/report-card.component';
import { SkillTrackerComponent } from './modules/performance/skill-tracker/skill-tracker.component';
import { MyDocumentsComponent } from './modules/career-assistance/my-documents/my-documents.component';
import { PlacementMonitorComponent } from './modules/career-assistance/placement-monitor/placement-monitor.component';
import { CompanyJdComponent } from './modules/career-assistance/company-job-descriptions/company-jd.component';
import { CodeOfConductComponent } from './modules/career-assistance/code-of-conduct/code-of-conduct.component';
import { FeedbackListComponent } from './modules/feedback/feedback-list/feedback-list.component';
import { FeedbackFormComponent } from './modules/feedback/feedback-form/feedback-form.component';
import { RaiseTicketComponent } from './modules/helpdesk/raise-ticket/raise-ticket.component';
import { HolidaysComponent } from './pages/holidays/holidays.component';
import { EnrollmentDetailsComponent } from './modules/profile/enrollment-details/enrollment-details.component';
import { McqTestEngineComponent } from './modules/assessment/mcq-test-engine/mcq-test-engine.component';

export const routes: Routes = [
  // Public route
  { path: 'login', component: Login },

  // The live MCQ test runs OUTSIDE the dashboard shell: it takes over the whole
  // viewport in fullscreen, with no sidebar or header to navigate away through.
  {
    path: 'assessment/mcq-test/attempt',
    component: McqTestEngineComponent,
    canActivate: [authGuard],
  },

  // Shell routes (MainLayout wrapper)
  {
    path: '',
    component: MainLayout,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'overview', pathMatch: 'full' },
      { path: 'overview', component: OverviewComponent },
      { path: 'profile/personal-info', component: PersonalInfoComponent },

      // FIXED: Added profile/ prefix here to match sidebar link
      { path: 'profile/billing-payments', component: BillingPaymentsComponent },
      { path: 'criteria/evaluation-policy', component: EvaluationPolicyComponent },
      { path: 'progress', component: ProgressComponent },
      { path: 'schedule/class-schedule', component: ClassScheduleComponent },
      { path: 'attendance/daily-attendance', component: DailyAttendanceComponent },
      { path: 'resources/study-materials', component: StudyMaterialsComponent },
      { path: 'resources/class-notes', component: ClassNotesComponent },
      { path: 'resources/reference-videos', component: ReferenceVideosComponent },
      { path: 'resources/digital-content', component: DigitalContentComponent },
      { path: 'tutorials/live-sessions-archive', component: LiveSessionsArchiveComponent },
      { path: 'practice/mcq-practice-tests', component: McqPracticeTestsComponent },
      { path: 'practice/mcq-practice-results', component: McqPracticeResultsComponent },
      { path: 'practice/programming-exercises', component: ProgrammingExercisesComponent },
      { path: 'practice/coding-practice', component: CodingPracticeComponent },
      { path: 'practice/industry-interview-questions', component: IndustryInterviewQuestionsComponent },
      { path: 'practice/programming-compiler', component: ProgrammingCompilerComponent },
      { path: 'assessment/quizzes-tests', component: QuizzesTestsComponent },
      { path: 'assessment/theory-lab-test', component: TheoryLabTestComponent },
      { path: 'assessment/upload-projects', component: UploadProjectsComponent },
      { path: 'assessment/results-reports', component: McqTestResultDetailsComponent },
      { path: 'performance/report-card', component: ReportCardComponent },
      { path: 'performance/skill-tracker', component: SkillTrackerComponent },
      { path: 'career-assistance/my-documents', component: MyDocumentsComponent},
      { path: 'career-assistance/placement-monitor', component: PlacementMonitorComponent},
      { path: 'career-assistance/company-job-descriptions', component: CompanyJdComponent},
      { path: 'career-assistance/code-of-conduct', component: CodeOfConductComponent},
      // One Feedback page. The old Submit/History split lives on only as
      // redirects, so existing links and bookmarks still land somewhere useful.
      { path: 'feedback', component: FeedbackListComponent},
      { path: 'feedback/feedback-form', component: FeedbackFormComponent},
      { path: 'feedback/submit-feedback', redirectTo: 'feedback', pathMatch: 'full'},
      { path: 'feedback/feedback-history', redirectTo: 'feedback', pathMatch: 'full'},
      { path: 'helpdesk/raise-ticket', component: RaiseTicketComponent},
      { path: 'holidays', component: HolidaysComponent },
      { path: 'profile/enrollment-details', component: EnrollmentDetailsComponent}
    ],
  },

  // Fallback route
  { path: '**', redirectTo: '' },
];