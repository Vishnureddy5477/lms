import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface TodayClassItem {
  module: string;
  trainer: string;
  startTime: string;
  endTime: string;
  status: string;
  linkActive: boolean;
  joinLink: string;
}

export interface WeekClassItem {
  module: string;
  trainer: string;
  startTime: string;
  endTime: string;
  startDate: string;
  endDate: string;
  mode: string;
  status: string;
}

export interface ScheduleResponse {
  todayClasses: TodayClassItem[];
  weekClasses: WeekClassItem[];
}

@Injectable({ providedIn: 'root' })
export class ScheduleService {
  constructor(private http: HttpClient) {}

  getSchedule(): Observable<ScheduleResponse> {
    return this.http.get<ScheduleResponse>(`${environment.apiUrl}/student/schedule`);
  }
}
