import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { McqPracticeResultItem, PracticeService } from '../../../services/practice.service';
import { SkeletonRowComponent } from '../../../shared/skeleton/skeleton-row.component';

@Component({
  selector: 'app-mcq-practice-results',
  standalone: true,
  imports: [CommonModule, SkeletonRowComponent],
  templateUrl: './mcq-practice-results.component.html',
  styleUrls: ['./mcq-practice-results.component.css']
})
export class McqPracticeResultsComponent implements OnInit {

  resultsList: McqPracticeResultItem[] = [];
  loading = true;

  constructor(private practiceService: PracticeService) { }

  ngOnInit(): void {
    this.practiceService.getMcqPracticeResults().subscribe({
      next: (list) => {
        this.resultsList = list;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

}
