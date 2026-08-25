import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface RecordedSessionItem {
  slNo: number;
  batch: string;
  subject: string;
  recordedLink: string;
  date: string;
  status: string;
}

@Injectable({ providedIn: 'root' })
export class TutorialsService {
  constructor(private http: HttpClient) {}

  getRecordedSessions(): Observable<RecordedSessionItem[]> {
    return this.http.get<RecordedSessionItem[]>(`${environment.apiUrl}/student/tutorials/recorded-sessions`);
  }
}
