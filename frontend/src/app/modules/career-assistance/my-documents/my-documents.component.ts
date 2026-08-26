import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DashboardService } from '../../../services/dashboard.service';
import { PlacementService } from '../../../services/placement.service';

export interface UserDocument {
  id: number;
  registrationNo: string;
  email: string;
  documentName: string;
  documentUrl: string;
}

@Component({
  selector: 'app-my-documents',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './my-documents.component.html',
  styleUrls: ['./my-documents.component.css']
})
export class MyDocumentsComponent implements OnInit {

  activeTab: 'resumeFormat' | 'uploadDoc' | 'viewDocs' = 'resumeFormat';

  uploadData = {
    registrationNo: '',
    batchNo: '',
    email: '',
    selectedDocType: '',
    file: null as File | null
  };

  documentTypes: string[] = [
    '10th Marks Card',
    '12th / Diploma Marks Card',
    'Graduation Marks Card',
    'Resume',
    'Project Report 1',
    'Project Report 2',
    'Project Report 3'
  ];

  uploadedDocuments: UserDocument[] = [];
  uploading = false;
  message = '';

  constructor(private dashboardService: DashboardService, private placementService: PlacementService) {}

  ngOnInit(): void {
    this.dashboardService.getProfile().subscribe({
      next: (profile) => {
        this.uploadData.registrationNo = profile.regNo;
        this.uploadData.batchNo = profile.batch;
        this.uploadData.email = profile.email;
      },
    });
    this.loadDocuments();
  }

  private loadDocuments(): void {
    this.placementService.listDocuments().subscribe({
      next: (docs) => {
        this.uploadedDocuments = docs.map((d) => ({
          id: d.id,
          registrationNo: this.uploadData.registrationNo,
          email: d.email,
          documentName: `View - ${d.docType}`,
          documentUrl: d.docLink,
        }));
      },
    });
  }

  setTab(tab: 'resumeFormat' | 'uploadDoc' | 'viewDocs'): void {
    this.activeTab = tab;
    this.message = '';
  }

  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (file) {
      this.uploadData.file = file;
    }
  }

  onSubmitUpload(): void {
    if (!this.uploadData.selectedDocType || !this.uploadData.file) {
      alert('Please fill in all required fields and choose a PDF file.');
      return;
    }

    this.uploading = true;
    this.placementService.uploadDocument(this.uploadData.selectedDocType, this.uploadData.file).subscribe({
      next: () => {
        this.uploading = false;
        alert('Document uploaded successfully!');
        this.uploadData.selectedDocType = '';
        this.uploadData.file = null;
        this.loadDocuments();
        this.setTab('viewDocs');
      },
      error: (err) => {
        this.uploading = false;
        alert(err?.error?.message || 'Failed to upload document.');
      },
    });
  }

  deleteDocument(id: number): void {
    if (!confirm('Are you sure you want to delete this file?')) {
      return;
    }
    this.placementService.deleteDocument(id).subscribe({
      next: () => this.loadDocuments(),
      error: (err) => alert(err?.error?.message || 'Failed to delete document.'),
    });
  }

  downloadFormat(): void {
    window.open('/images/resume-format-new.pdf', '_blank');
  }

  viewDocument(doc: UserDocument): void {
    window.open(doc.documentUrl, '_blank');
  }

}
