import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface TicketSubmitResponse {
  success: boolean;
  message: string;
  ticketId: string;
}

@Injectable({ providedIn: 'root' })
export class HelpdeskService {
  constructor(private http: HttpClient) {}

  submitTicket(identityOption: string, subject: string, concernDescription: string, file: File | null): Observable<TicketSubmitResponse> {
    const formData = new FormData();
    formData.append('identityOption', identityOption);
    formData.append('subject', subject);
    formData.append('concernDescription', concernDescription);
    if (file) {
      formData.append('file', file);
    }
    return this.http.post<TicketSubmitResponse>(`${environment.apiUrl}/student/helpdesk/tickets`, formData);
  }
}
