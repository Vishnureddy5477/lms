import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface McqPracticeTestItem {
  moduleName: string;
  chapter: string;
  questionsCount: number;
  duration: string;
}

export interface McqPracticeResultItem {
  slNo: number;
  registrationNo: string;
  moduleName: string;
  chapter: string;
  totalCorrect: number;
  totalIncorrect: number;
  totalNotAttempt: number;
  percentage: string;
  examDatetime: string;
}

export interface ProgrammingExerciseItem {
  id: number;
  title: string;
  link: string;
}

export interface InterviewCompanyModuleItem {
  companyName: string;
  moduleCategory: string;
  moduleName: string;
  questionCount: number;
}

export interface InterviewQuestionItem {
  id: number;
  moduleCategory: string;
  moduleName: string;
  question: string;
  answerCount: number;
}

export interface InterviewAnswerItem {
  answerId: number;
  answerText: string;
  ansCount: number;
  createdBy: string;
  createdAt: string;
}

export interface CodeRunRequest {
  code: string;
  language: string;
  input: string;
}

export interface CodeRunOutput {
  stdout: string;
  stderr: string;
  code: number;
  signal: string | null;
  output: string;
}

export interface CodeRunResult {
  language: string;
  version: string;
  run: CodeRunOutput;
  compile?: CodeRunOutput;
}

@Injectable({ providedIn: 'root' })
export class PracticeService {
  constructor(private http: HttpClient) {}

  getMcqPracticeTests(): Observable<McqPracticeTestItem[]> {
    return this.http.get<McqPracticeTestItem[]>(`${environment.apiUrl}/student/practice/mcq-tests`);
  }

  getMcqPracticeResults(): Observable<McqPracticeResultItem[]> {
    return this.http.get<McqPracticeResultItem[]>(`${environment.apiUrl}/student/practice/mcq-results`);
  }

  getProgrammingExercises(): Observable<ProgrammingExerciseItem[]> {
    return this.http.get<ProgrammingExerciseItem[]>(`${environment.apiUrl}/student/practice/programming-exercises`);
  }

  getInterviewCompanyModules(): Observable<InterviewCompanyModuleItem[]> {
    return this.http.get<InterviewCompanyModuleItem[]>(`${environment.apiUrl}/student/practice/interview-questions/companies`);
  }

  getInterviewQuestions(moduleCategory: string): Observable<InterviewQuestionItem[]> {
    return this.http.get<InterviewQuestionItem[]>(`${environment.apiUrl}/student/practice/interview-questions`, {
      params: { moduleCategory },
    });
  }

  getInterviewAnswers(questionId: number): Observable<InterviewAnswerItem[]> {
    return this.http.get<InterviewAnswerItem[]>(`${environment.apiUrl}/student/practice/interview-questions/${questionId}/answers`);
  }

  runCode(payload: CodeRunRequest): Observable<CodeRunResult> {
    return this.http.post<CodeRunResult>(`${environment.apiUrl}/compiler/run`, payload);
  }
}
