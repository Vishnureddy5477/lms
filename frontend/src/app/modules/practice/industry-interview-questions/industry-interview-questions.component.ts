import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  InterviewAnswerItem,
  InterviewCompanyModuleItem,
  InterviewQuestionItem,
  PracticeService,
} from '../../../services/practice.service';
import { SkeletonComponent } from '../../../shared/skeleton/skeleton.component';
import { SkeletonRowComponent } from '../../../shared/skeleton/skeleton-row.component';

@Component({
  selector: 'app-industry-interview-questions',
  standalone: true,
  imports: [CommonModule, SkeletonComponent, SkeletonRowComponent],
  templateUrl: './industry-interview-questions.component.html',
  styleUrls: ['./industry-interview-questions.component.css']
})
export class IndustryInterviewQuestionsComponent implements OnInit {

  questionsList: InterviewCompanyModuleItem[] = [];
  loading = true;

  selectedModuleCategory: string | null = null;
  drilldownQuestions: InterviewQuestionItem[] = [];
  loadingQuestions = false;

  activeQuestion: InterviewQuestionItem | null = null;
  activeAnswers: InterviewAnswerItem[] = [];
  loadingAnswers = false;

  constructor(private practiceService: PracticeService) { }

  ngOnInit(): void {
    this.practiceService.getInterviewCompanyModules().subscribe({
      next: (list) => {
        this.questionsList = list;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  onViewQuestions(item: InterviewCompanyModuleItem): void {
    this.selectedModuleCategory = item.moduleCategory;
    this.loadingQuestions = true;
    this.drilldownQuestions = [];

    this.practiceService.getInterviewQuestions(item.moduleCategory).subscribe({
      next: (list) => {
        this.drilldownQuestions = list;
        this.loadingQuestions = false;
      },
      error: () => {
        this.loadingQuestions = false;
      },
    });
  }

  backToCompanies(): void {
    this.selectedModuleCategory = null;
    this.drilldownQuestions = [];
  }

  onViewAnswers(question: InterviewQuestionItem): void {
    this.activeQuestion = question;
    this.loadingAnswers = true;
    this.activeAnswers = [];

    this.practiceService.getInterviewAnswers(question.id).subscribe({
      next: (list) => {
        this.activeAnswers = list;
        this.loadingAnswers = false;
      },
      error: () => {
        this.loadingAnswers = false;
      },
    });
  }

  closeAnswers(): void {
    this.activeQuestion = null;
    this.activeAnswers = [];
  }

}
