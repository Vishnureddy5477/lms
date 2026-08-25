import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

  courseInfo = {
    studentName: 'LMS',
    batchNumber: 'abc',
    courseName: 'Advanced Diploma in Embedded Systems',
    startDate: '2023-12-16'
  };

  jobMetrics = {
    totalOpportunity: 0,
    totalApplied: 0,
    totalLost: 0
  };

  constructor() { }

  ngOnInit(): void {
  }

}