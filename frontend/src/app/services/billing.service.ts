import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface BillingRecord {
  receiptNo: number;
  receiptDate: string;
  studentName: string;
  paid: number;
  due: number;
  conseller: string;
  enquiryNo: string;
  course: string;
  paymentMode: string;
  courseFee: number;
  installmentNo: number;
  admissionFee: number;
  receivedBy: string;
  batchNo: string;
}

export interface BillingHistory {
  records: BillingRecord[];
  totalDues: number;
}

@Injectable({ providedIn: 'root' })
export class BillingService {
  constructor(private http: HttpClient) {}

  getHistory(): Observable<BillingHistory> {
    return this.http.get<BillingHistory>(`${environment.apiUrl}/student/billing/history`);
  }
}
