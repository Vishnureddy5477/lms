import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  EvaluationPolicyService as EvaluationPolicyApi,
  ModuleEvaluationRow,
  PlacementEvaluationRow,
} from '../../../services/evaluation-policy.service';
import { SkeletonComponent } from '../../../shared/skeleton/skeleton.component';

@Component({
  selector: 'app-evaluation-policy',
  standalone: true,
  imports: [CommonModule, SkeletonComponent],
  templateUrl: './evaluation-policy.component.html',
  styleUrls: ['./evaluation-policy.component.css']
})
export class EvaluationPolicyComponent implements OnInit {

  activeTab: string = 'module';
  loading = true;

  // 1. Module Evaluation Data — global reference table, same for every student
  moduleCriteria: ModuleEvaluationRow[] = [];

  // 2. Placement Evaluation Data — global reference table, same for every student
  placementCriteria: PlacementEvaluationRow[] = [];

  // 3. Technical Mock — evaluation criteria (dimensions the trainer scores 1–5 on)
  technicalMockCriteria = [
    { slNo: 1, criterion: 'Communication & Confidence' },
    { slNo: 2, criterion: 'Resume Standard' },
    { slNo: 3, criterion: 'Fundamental Concepts' },
    { slNo: 4, criterion: 'Problem Solving Ability' },
    { slNo: 5, criterion: 'Coding Skills' },
    { slNo: 6, criterion: 'Industry Application Awareness' },
    { slNo: 7, criterion: 'Project Level & Explaining' },
    { slNo: 8, criterion: 'Technical Competency' },
  ];

  // 4. HR Mock — evaluation criteria (dimensions the HR panel scores 1–5 on)
  hrMockCriteria = [
    { slNo: 1, criterion: 'Resume' },
    { slNo: 2, criterion: 'Communication Skills' },
    { slNo: 3, criterion: 'Professionalism' },
    { slNo: 4, criterion: 'Self Introduction' },
    { slNo: 5, criterion: 'Strengths and Weaknesses' },
    { slNo: 6, criterion: 'Confidence Level' },
    { slNo: 7, criterion: 'Adaptability and Teamwork' },
    { slNo: 8, criterion: 'Career Goals' },
  ];

  // 5. Initials & Values Data
  mockInitialsData = [
    { slNo: 1, initials: 'P', rating: 'Poor', marks: 0 },
    { slNo: 2, initials: 'A', rating: 'Average', marks: 3 },
    { slNo: 3, initials: 'G', rating: 'Good', marks: 4 },
    { slNo: 4, initials: 'VG', rating: 'Very Good', marks: 5 }
  ];

  constructor(private evaluationPolicyApi: EvaluationPolicyApi) { }

  ngOnInit(): void {
    this.evaluationPolicyApi.getPolicy().subscribe({
      next: (res) => {
        this.moduleCriteria = res.moduleCriteria;
        this.placementCriteria = res.placementCriteria;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  setTab(tab: string): void {
    this.activeTab = tab;
  }

}