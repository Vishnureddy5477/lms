import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AssessmentService } from '../../../services/assessment.service';
import { SkeletonComponent } from '../../../shared/skeleton/skeleton.component';

export interface TheoryLabTestItem {
  id: string;
  testTitle: string;
  testType: 'Theory' | 'Lab';
  scheduledDate: string;
  duration: string;
}

@Component({
  selector: 'app-theory-lab-test',
  standalone: true,
  imports: [CommonModule, SkeletonComponent],
  templateUrl: './theory-lab-test.component.html',
  styleUrls: ['./theory-lab-test.component.css']
})
export class TheoryLabTestComponent implements OnInit {

  scheduledTests: TheoryLabTestItem[] = [];
  isLoading = false;

  constructor(private assessmentService: AssessmentService) { }

  ngOnInit(): void {
    this.isLoading = true;
    this.assessmentService.getTheoryTests().subscribe({
      next: (tests) => {
        this.scheduledTests = tests.map((t) => ({
          id: t.theoryTestId,
          testTitle: t.moduleName,
          testType: t.testType.toLowerCase() === 'lab' ? 'Lab' : 'Theory',
          scheduledDate: t.testDate,
          duration: `${t.testStartTime} - ${t.testEndTime}`,
        }));
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

}
