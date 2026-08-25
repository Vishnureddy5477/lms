import { Component, OnInit } from '@angular/core';
import { DashboardService } from '../../../services/dashboard.service';
import { BillingService, BillingRecord } from '../../../services/billing.service';
import { SkeletonComponent } from '../../../shared/skeleton/skeleton.component';

const RAZORPAY_PAYMENT_URL = 'https://pages.razorpay.com/cranes-varsity';

@Component({
  selector: 'app-billing-payments',
  standalone: true,
  imports: [SkeletonComponent],
  templateUrl: './billing-payments.component.html',
  styleUrls: ['./billing-payments.component.css']
})
export class BillingPaymentsComponent implements OnInit {

  studentName = '';
  regNumber = '';

  billingRecords: BillingRecord[] = [];
  totalDues = 0;
  loading = true;

  constructor(private dashboardService: DashboardService, private billingService: BillingService) {}

  ngOnInit(): void {
    this.dashboardService.getProfile().subscribe({
      next: (profile) => {
        this.studentName = profile.name;
        this.regNumber = profile.regNo;
      },
    });

    this.billingService.getHistory().subscribe({
      next: (history) => {
        this.billingRecords = history.records;
        this.totalDues = history.totalDues;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  onPayCourseFee(): void {
    window.open(RAZORPAY_PAYMENT_URL, '_blank');
  }

  printReceipt(record: BillingRecord): void {
    const printWindow = window.open('', '_blank');
    if (!printWindow) {
      return;
    }

    printWindow.document.write(`
      <html>
        <head>
          <title>Fee Receipt - ${record.receiptNo}</title>
          <style>
            body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #555; }
            table { max-width: 800px; margin: auto; border-collapse: collapse; width: 100%; }
            td, th { padding: 8px; border: 1px solid #eee; text-align: left; }
            .title { text-align: center; background: whitesmoke; font-weight: bold; }
          </style>
        </head>
        <body>
          <table>
            <tr><td colspan="4" class="title">Fee Receipt</td></tr>
            <tr><td>Receipt No.</td><td>${record.receiptNo}</td><td>Receipt Date</td><td>${record.receiptDate}</td></tr>
            <tr><td>Conseller Name</td><td>${record.conseller ?? ''}</td><td>Enquiry No.</td><td>${record.enquiryNo ?? ''}</td></tr>
            <tr><td>Registration No.</td><td>${this.regNumber}</td><td>Course Name</td><td>${record.course ?? ''}</td></tr>
            <tr><td>Student Name</td><td>${record.studentName ?? ''}</td><td>Payment Mode</td><td>${record.paymentMode ?? ''}</td></tr>
            <tr><td>Batch No.</td><td>${record.batchNo ?? ''}</td><td>Installment No.</td><td>${record.installmentNo}</td></tr>
            <tr><td>Course Fee</td><td>₹${record.courseFee}</td><td>Admission Fee</td><td>₹${record.admissionFee}</td></tr>
            <tr><td>Paid</td><td>₹${record.paid}</td><td>Due</td><td>₹${record.due}</td></tr>
            <tr><td colspan="2">Received By</td><td colspan="2">${record.receivedBy ?? ''}</td></tr>
          </table>
        </body>
      </html>
    `);
    printWindow.document.close();
    printWindow.focus();
    printWindow.print();
  }
}
