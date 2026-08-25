import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface Holiday {
  id: number;
  title: string;
  date: string;
  day: string;
}

@Component({
  selector: 'app-holidays',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './holidays.component.html',
  styleUrls: ['./holidays.component.css']
})
export class HolidaysComponent {
  pageSize = 10;
  currentPage = 1;
  searchTerm = '';
  sortColumn: keyof Holiday = 'id';
  sortAscending = true;

  holidays: Holiday[] = [
    { id: 1, title: 'Makara Sankranti Festival', date: '2026-01-15', day: 'Thursday' },
    { id: 2, title: 'Republic Day', date: '2026-01-26', day: 'Monday' },
    { id: 3, title: 'Ugadi', date: '2026-03-19', day: 'Thursday' },
    { id: 4, title: 'May Day', date: '2026-05-01', day: 'Friday' },
    { id: 5, title: 'Bakrid', date: '2026-05-28', day: 'Thursday' },
    { id: 6, title: 'Ganesh Chaturthi', date: '2026-09-14', day: 'Monday' },
    { id: 7, title: 'Gandhi Jayanthi', date: '2026-10-02', day: 'Friday' },
    { id: 8, title: 'Vijaya Dasami', date: '2026-10-21', day: 'Wednesday' },
    { id: 9, title: 'Balipadyami, Deepavali', date: '2026-11-10', day: 'Tuesday' },
    { id: 10, title: 'Christmas', date: '2026-12-25', day: 'Friday' }
  ];

  // Filters, sorts, and slices items based on user inputs
  get filteredHolidays(): Holiday[] {
    let result = this.holidays;

    // Filter
    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase();
      result = result.filter(h =>
        h.title.toLowerCase().includes(term) ||
        h.date.toLowerCase().includes(term) ||
        h.day.toLowerCase().includes(term)
      );
    }

    // Sort
    result = [...result].sort((a, b) => {
      const valA = a[this.sortColumn];
      const valB = b[this.sortColumn];
      if (valA < valB) return this.sortAscending ? -1 : 1;
      if (valA > valB) return this.sortAscending ? 1 : -1;
      return 0;
    });

    // Paginate
    const startIndex = (this.currentPage - 1) * this.pageSize;
    return result.slice(startIndex, startIndex + Number(this.pageSize));
  }

  // Toggle sorting by column
  sortBy(column: keyof Holiday): void {
    if (this.sortColumn === column) {
      this.sortAscending = !this.sortAscending;
    } else {
      this.sortColumn = column;
      this.sortAscending = true;
    }
  }

  // Calculate total active pages
  get totalPages(): number {
    return Math.ceil(this.holidays.length / this.pageSize) || 1;
  }
}