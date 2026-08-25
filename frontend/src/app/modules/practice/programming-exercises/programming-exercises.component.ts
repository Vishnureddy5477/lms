import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PracticeService, ProgrammingExerciseItem } from '../../../services/practice.service';
import { SkeletonComponent } from '../../../shared/skeleton/skeleton.component';

@Component({
  selector: 'app-programming-exercises',
  standalone: true,
  imports: [CommonModule, SkeletonComponent],
  templateUrl: './programming-exercises.component.html',
  styleUrls: ['./programming-exercises.component.css']
})
export class ProgrammingExercisesComponent implements OnInit {

  topicsList: ProgrammingExerciseItem[] = [];
  loading = true;

  constructor(private practiceService: PracticeService) { }

  ngOnInit(): void {
    this.practiceService.getProgrammingExercises().subscribe({
      next: (list) => {
        this.topicsList = list;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  onViewQuestions(topic: ProgrammingExerciseItem): void {
    window.open(topic.link, '_blank');
  }

}
