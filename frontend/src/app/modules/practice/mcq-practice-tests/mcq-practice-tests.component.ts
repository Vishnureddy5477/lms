import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { McqPracticeTestItem, PracticeService } from '../../../services/practice.service';
import { SkeletonRowComponent } from '../../../shared/skeleton/skeleton-row.component';

@Component({
  selector: 'app-mcq-practice-tests',
  standalone: true,
  imports: [CommonModule, FormsModule, SkeletonRowComponent],
  templateUrl: './mcq-practice-tests.component.html',
  styleUrls: ['./mcq-practice-tests.component.css']
})
export class McqPracticeTestsComponent implements OnInit {

  selectedModuleFilter: string = 'Select A Module';
  moduleOptions: string[] = ['Select A Module'];

  allTestsList: McqPracticeTestItem[] = [];
  displayedTestsList: McqPracticeTestItem[] = [];
  loading = true;

  constructor(private practiceService: PracticeService) { }

  ngOnInit(): void {
    this.practiceService.getMcqPracticeTests().subscribe({
      next: (list) => {
        this.allTestsList = list;
        this.displayedTestsList = [...list];
        this.moduleOptions = ['Select A Module', ...Array.from(new Set(list.map(t => t.moduleName)))];
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  onGetDetails(): void {
    if (this.selectedModuleFilter === 'Select A Module' || !this.selectedModuleFilter) {
      this.displayedTestsList = [...this.allTestsList];
    } else {
      this.displayedTestsList = this.allTestsList.filter(
        test => test.moduleName.toLowerCase() === this.selectedModuleFilter.toLowerCase()
      );
    }
  }

  onTakeTest(test: McqPracticeTestItem): void {
    alert('Taking practice tests isn\'t available yet. Check back soon!');
  }

}
