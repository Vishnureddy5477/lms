import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DashboardService } from '../../../services/dashboard.service';
import { AssessmentService } from '../../../services/assessment.service';

export interface UploadedProject {
  id: number;
  projectType: string;
  projectTitle: string;
  uploadDate: string;
  documentName: string;
  documentLink: string;
}

// Matches the legacy 5 project types exactly (UploadProjectDocument.jsp doctype codes 1-5).
const PROJECT_TYPE_CODES: Record<string, string> = {
  'Cranes Programming Project': '1',
  'Cranes Hardware Project': '2',
  'Specialization': '3',
  'College Project': '4',
  'Other Project/Internship': '5',
};

@Component({
  selector: 'app-upload-projects',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './upload-projects.component.html',
  styleUrls: ['./upload-projects.component.css']
})
export class UploadProjectsComponent implements OnInit {

  registrationNumber = '';
  batch = '';
  selectedProjectType = '';
  projectTitle = '';
  selectedFile: File | null = null;
  errorMessage = '';
  submitting = false;

  projectTypes: string[] = Object.keys(PROJECT_TYPE_CODES);
  uploadedProjects: UploadedProject[] = [];

  constructor(private dashboardService: DashboardService, private assessmentService: AssessmentService) {}

  ngOnInit(): void {
    this.dashboardService.getProfile().subscribe({
      next: (profile) => {
        this.registrationNumber = profile.regNo;
        this.batch = profile.batch;
      },
    });
    this.loadProjects();
  }

  private loadProjects(): void {
    this.assessmentService.listProjects().subscribe({
      next: (projects) => {
        this.uploadedProjects = projects.map((p, i) => ({
          id: i,
          projectType: p.type,
          projectTitle: p.title,
          uploadDate: p.date,
          documentName: 'View PDF',
          documentLink: p.link,
        }));
      },
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
      this.errorMessage = '';
    }
  }

  onSubmit(): void {
    if (!this.selectedProjectType) {
      this.errorMessage = 'Please select a project type.';
      return;
    }
    if (!this.projectTitle.trim()) {
      this.errorMessage = 'Please enter a project title.';
      return;
    }
    if (!this.selectedFile) {
      this.errorMessage = 'Please upload a PDF document.';
      return;
    }
    if (this.selectedFile.type !== 'application/pdf') {
      this.errorMessage = 'Only PDF files are allowed.';
      return;
    }
    if (this.selectedFile.size > 10 * 1024 * 1024) {
      this.errorMessage = 'File size should not exceed 10MB.';
      return;
    }

    const code = PROJECT_TYPE_CODES[this.selectedProjectType];
    this.errorMessage = '';
    this.submitting = true;

    this.assessmentService.uploadProject(code, this.projectTitle.trim(), this.selectedFile).subscribe({
      next: (res) => {
        this.submitting = false;
        alert(res.message);
        this.selectedProjectType = '';
        this.projectTitle = '';
        this.selectedFile = null;
        this.loadProjects();
      },
      error: (err) => {
        this.submitting = false;
        this.errorMessage = err?.error?.message || 'Upload failed. Please try again.';
      },
    });
  }

}
