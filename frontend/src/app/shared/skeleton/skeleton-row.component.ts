import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

/**
 * A skeleton row for use INSIDE an existing table body.
 *
 * The selector is `tr[appSkeletonRow]` on purpose: a normal component host
 * element (<app-skeleton>) between <tbody> and <tr> is invalid markup, and the
 * browser hoists it out of the table, which breaks the layout. Attaching the
 * component to the <tr> itself keeps the table structure valid.
 *
 * Usage — replaces the old `<tr><td colspan="4">Loading...</td></tr>`:
 *   @for (r of [1,2,3,4,5]; track r) {
 *     <tr appSkeletonRow [columns]="4"></tr>
 *   }
 */
@Component({
  selector: 'tr[appSkeletonRow]',
  standalone: true,
  imports: [CommonModule],
  template: `
    @for (c of cols; track c) {
      <td><span class="sk"></span></td>
    }
  `,
  styles: [`
    td { padding: 0.75rem 0.5rem; vertical-align: middle; }

    .sk {
      display: block;
      height: 12px;
      border-radius: 6px;
      background: #e9ecf3;
      background-image: linear-gradient(90deg, #e9ecf3 0%, #f4f6fa 50%, #e9ecf3 100%);
      background-size: 200% 100%;
      animation: sk-shimmer 1.3s ease-in-out infinite;
    }

    @keyframes sk-shimmer {
      0%   { background-position: 200% 0; }
      100% { background-position: -200% 0; }
    }

    @media (prefers-reduced-motion: reduce) {
      .sk { animation: none; }
    }
  `],
})
export class SkeletonRowComponent {
  @Input() columns = 4;

  get cols(): number[] {
    return Array.from({ length: Math.max(1, this.columns) }, (_, i) => i);
  }
}
