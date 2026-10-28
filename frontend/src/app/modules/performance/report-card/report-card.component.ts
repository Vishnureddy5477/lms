import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../../services/dashboard.service';
import {
  MarkComponent as Mark,
  MarkTotals,
  ModuleMarksRow,
  ReportCardService,
} from '../../../services/report-card.service';
import { SkeletonComponent } from '../../../shared/skeleton/skeleton.component';

export interface StudentDetails {
  studentRegistrationNo: string;
  studentName: string;
  program: string;
}

const LOGO_URL = 'assets/images/cranes-varsity-logo.png';
const HEADER_BLUE: [number, number, number] = [68, 114, 196];
const DASH = '—';

@Component({
  selector: 'app-report-card',
  standalone: true,
  imports: [CommonModule, SkeletonComponent],
  templateUrl: './report-card.component.html',
  styleUrls: ['./report-card.component.css']
})
export class ReportCardComponent implements OnInit {

  readonly logoUrl = LOGO_URL;

  student: StudentDetails = {
    studentRegistrationNo: '',
    studentName: '',
    program: ''
  };

  modules: ModuleMarksRow[] = [];
  totals: MarkTotals | null = null;
  loading = true;
  downloading = false;
  downloadError = '';

  // Two rows of three, exactly as printed on the card. The bands must match
  // the backend's gradeFor().
  readonly gradingRows = [
    ['A+ [Excellent 90% and above]', 'A [Good 80% to 89%]', 'B+ [Average 70% to 79%]'],
    ['B [Below Average 60% to 69%]', 'C [Poor 50% to 59%]', 'Fail [Below 50%]'],
  ];

  constructor(private dashboardService: DashboardService, private reportCardService: ReportCardService) {}

  ngOnInit(): void {
    this.dashboardService.getProfile().subscribe({
      next: (profile) => {
        this.student.studentRegistrationNo = profile.regNo;
        this.student.studentName = profile.name;
      },
    });

    this.reportCardService.getReportCard().subscribe({
      next: (reportCard) => {
        this.student.program = reportCard.course;
        this.modules = reportCard.modules;
        this.totals = reportCard.totals;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  /** A component with no mark — not held, or not yet entered — prints as a dash. */
  cell(mark: Mark): string {
    return mark.conducted && mark.recorded ? this.fixed(mark.obtained) : DASH;
  }

  /** Sum of the marks actually entered for the row; a dash when there are none. */
  rowTotal(row: ModuleMarksRow): string {
    const parts = [row.mcq, row.theory, row.lab, row.assignment, row.project]
      .filter(m => m.conducted && m.recorded);
    if (parts.length === 0) {
      return DASH;
    }
    return this.fixed(parts.reduce((sum, m) => sum + Number(m.obtained ?? 0), 0));
  }

  get footerObtained(): string {
    return this.totals?.percentage != null ? this.whole(this.totals.totalObtained) : DASH;
  }

  get footerMax(): string {
    return this.totals?.percentage != null ? this.whole(this.totals.totalMax) : DASH;
  }

  get footerPercentage(): string {
    return this.totals?.percentage != null ? `${Math.round(Number(this.totals.percentage))}%` : DASH;
  }

  /** "B+ (Average)" from the backend prints as "B+", as on the card. */
  get footerGrade(): string {
    const grade = this.totals?.grade;
    return grade ? grade.split(' (')[0] : DASH;
  }

  private fixed(value: number | null): string {
    return value === null || value === undefined ? DASH : Number(value).toFixed(2);
  }

  private whole(value: number): string {
    const n = Number(value);
    return Number.isInteger(n) ? String(n) : n.toFixed(2);
  }

  async downloadPdf(): Promise<void> {
    this.downloading = true;
    this.downloadError = '';
    try {
      // Loaded on demand so the PDF library only reaches students who use it.
      const [{ jsPDF }, { default: autoTable }, logo] = await Promise.all([
        import('jspdf'),
        import('jspdf-autotable'),
        this.loadLogo(),
      ]);

      const doc = new jsPDF({ unit: 'mm', format: 'a4', compress: true });
      const left = 18;
      const width = 175;
      const right = left + width;

      doc.addImage(logo, 'PNG', 36, 22, 138, 31, 'logo', 'FAST');

      doc.setFont('helvetica', 'bold');
      doc.setFontSize(10);
      doc.text(`Registration No: ${this.student.studentRegistrationNo}`, left, 62);
      doc.text(`Name : ${this.student.studentName}`, right, 62, { align: 'right' });
      doc.text(`Certification In : ${this.student.program}`, 105, 70, { align: 'center' });

      const body = this.modules.map(row => [
        String(row.sNo), row.moduleName,
        this.cell(row.mcq), this.cell(row.theory), this.cell(row.lab), this.cell(row.assignment),
        this.rowTotal(row),
      ]);

      autoTable(doc, {
        startY: 76,
        margin: { left, right: 210 - right },
        tableWidth: width,
        theme: 'grid',
        head: [['SL NO', 'Module Name', 'MCQ\nMarks 20', 'Theory\nMarks 25', 'Lab Max\nMarks 25', 'Assignments', 'Total\nObtained\nMarks']],
        body,
        foot: [['', 'FINAL GRADE', this.footerObtained, this.footerMax, this.footerPercentage,
          { content: 'Grade', styles: { halign: 'right' } }, this.footerGrade]],
        showFoot: 'lastPage',
        styles: {
          font: 'helvetica', fontSize: 9.5, halign: 'center', valign: 'middle',
          textColor: [0, 0, 0], lineColor: [0, 0, 0], lineWidth: 0.2, cellPadding: 1.2,
        },
        headStyles: {
          fillColor: HEADER_BLUE, textColor: [255, 255, 255], fontStyle: 'bold', fontSize: 9,
          cellPadding: { top: 1.2, bottom: 1.2, left: 0.5, right: 0.5 },
        },
        footStyles: { fillColor: HEADER_BLUE, textColor: [255, 255, 255], fontStyle: 'bold' },
        columnStyles: {
          0: { cellWidth: 20.6 }, 1: { cellWidth: 46.2 }, 2: { cellWidth: 20.6 }, 3: { cellWidth: 21.7 },
          4: { cellWidth: 21.3 }, 5: { cellWidth: 22.9 }, 6: { cellWidth: 21.7 },
        },
      });

      const tableEnd = (doc as unknown as { lastAutoTable: { finalY: number } }).lastAutoTable.finalY;

      autoTable(doc, {
        startY: tableEnd + 6,
        margin: { left, right: 210 - right },
        tableWidth: width,
        theme: 'grid',
        body: this.gradingRows,
        styles: {
          font: 'helvetica', fontSize: 9.5, fontStyle: 'bold', halign: 'center', valign: 'middle',
          textColor: [0, 0, 0], lineColor: [0, 0, 0], lineWidth: 0.3, cellPadding: 2.2,
        },
        columnStyles: { 0: { cellWidth: 47.4 }, 1: { cellWidth: 61.8 }, 2: { cellWidth: 65.8 } },
      });

      const name = this.student.studentRegistrationNo || 'student';
      doc.save(`Report-Card-${name}.pdf`);
    } catch {
      this.downloadError = 'Could not create the PDF. Please try again.';
    } finally {
      this.downloading = false;
    }
  }

  private async loadLogo(): Promise<string> {
    const blob = await fetch(LOGO_URL).then(r => {
      if (!r.ok) {
        throw new Error(`Logo request failed: ${r.status}`);
      }
      return r.blob();
    });
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result as string);
      reader.onerror = () => reject(reader.error);
      reader.readAsDataURL(blob);
    });
  }
}
