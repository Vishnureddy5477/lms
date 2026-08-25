import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HelpdeskService } from '../../../services/helpdesk.service';

@Component({
  selector: 'app-raise-ticket',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './raise-ticket.component.html',
  styleUrls: ['./raise-ticket.component.css']
})
export class RaiseTicketComponent implements OnInit {

  // Ticket Form Model
  ticketData = {
    identityOption: 'Registration Number' as 'Registration Number' | 'Batch Number (Anonymous)',
    subject: '',
    concernDescription: '',
    attachment: null as File | null
  };

  submitting = false;

  constructor(private helpdeskService: HelpdeskService) {}

  ngOnInit(): void {}

  // File Picker Handler
  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (file) {
      if (file.type !== 'application/pdf') {
        alert('Please upload a PDF file only.');
        event.target.value = '';
        return;
      }
      if (file.size > 16 * 1024 * 1024) {
        alert('File size should not exceed 16MB.');
        event.target.value = '';
        return;
      }
      this.ticketData.attachment = file;
    }
  }

  // Submit Ticket
  onSubmitTicket(formRef?: HTMLFormElement): void {
    if (!this.ticketData.subject.trim()) {
      alert('Please enter a subject.');
      return;
    }

    if (!this.ticketData.concernDescription.trim()) {
      alert('Please enter your concern description.');
      return;
    }

    this.submitting = true;
    this.helpdeskService
      .submitTicket(this.ticketData.identityOption, this.ticketData.subject, this.ticketData.concernDescription, this.ticketData.attachment)
      .subscribe({
        next: (res) => {
          this.submitting = false;
          alert(res.message);
          this.ticketData = {
            identityOption: 'Registration Number',
            subject: '',
            concernDescription: '',
            attachment: null
          };
          formRef?.reset();
        },
        error: (err) => {
          this.submitting = false;
          alert(err?.error?.message || 'Failed to submit ticket. Please try again.');
        },
      });
  }

}