import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  SkillTrackerService,
  SkillTrackerMcqRow,
  SkillTrackerMarksRow,
  SkillTrackerMockRow,
} from '../../../services/skill-tracker.service';
import { SkeletonComponent } from '../../../shared/skeleton/skeleton.component';

export interface MockRatingCriteria {
  sNo: number;
  averageMarksRange: string;
  result: string;
  description: string;
}

const MOCK_RATING_CRITERIA: MockRatingCriteria[] = [
  { sNo: 1, averageMarksRange: '4.5 - 5.0', result: 'Excellent', description: 'Can write, debug, and optimize programs independently' },
  { sNo: 2, averageMarksRange: '4.0 - 4.4', result: 'Very Good', description: 'Understands all concepts, needs minor guidance' },
  { sNo: 3, averageMarksRange: '3.0 - 3.9', result: 'Good', description: 'Can write basic programs, struggles with complex logic' },
  { sNo: 4, averageMarksRange: '2.0 - 2.9', result: 'Average', description: 'Understands syntax but weak in logic and debugging' },
  { sNo: 5, averageMarksRange: '1.0 - 1.9', result: 'Below Average', description: 'Needs foundation-level retraining' },
];

@Component({
  selector: 'app-skill-tracker',
  standalone: true,
  imports: [CommonModule, SkeletonComponent],
  templateUrl: './skill-tracker.component.html',
  styleUrls: ['./skill-tracker.component.css']
})
export class SkillTrackerComponent implements OnInit {

  activeTab: 'mcq' | 'theory' | 'lab' | 'project' | 'mock' = 'mcq';
  mockSubTab: 'programming' | 'hardware' = 'programming';

  loading = true;
  errorMessage = '';

  mcqResults: SkillTrackerMcqRow[] = [];
  theoryResults: SkillTrackerMarksRow[] = [];
  labResults: SkillTrackerMarksRow[] = [];
  projectResults: SkillTrackerMarksRow[] = [];
  programmingMockResults: SkillTrackerMockRow[] = [];
  hardwareMockResults: SkillTrackerMockRow[] = [];

  mockCriteria = MOCK_RATING_CRITERIA;

  constructor(private skillTrackerService: SkillTrackerService) {}

  ngOnInit(): void {
    this.skillTrackerService.getSkillTracker().subscribe({
      next: (res) => {
        this.mcqResults = res.mcqResults;
        this.theoryResults = res.theoryResults;
        this.labResults = res.labResults;
        this.projectResults = res.projectResults;
        this.programmingMockResults = res.programmingMockResults;
        this.hardwareMockResults = res.hardwareMockResults;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load Skill Tracker data. Please try again later.';
        this.loading = false;
      },
    });
  }

  setTab(tab: 'mcq' | 'theory' | 'lab' | 'project' | 'mock'): void {
    this.activeTab = tab;
  }

  setMockSubTab(subTab: 'programming' | 'hardware'): void {
    this.mockSubTab = subTab;
  }

  get activeMockResults(): SkillTrackerMockRow[] {
    return this.mockSubTab === 'programming' ? this.programmingMockResults : this.hardwareMockResults;
  }

  get activeMockLabel(): string {
    return this.mockSubTab === 'programming' ? 'Programming Mock' : 'Hardware Mock';
  }

}
