import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

/**
 * Placeholder shown while a page's data is in flight.
 *
 * Worth knowing why this matters here specifically: the backend talks to an RDS
 * in another AWS region, so a round trip from a dev machine is ~275ms, and any
 * page that fetches per-row detail multiplies that. A skeleton does not make the
 * wait shorter — it makes it legible, showing the shape of what is coming
 * instead of a dead "Loading…".
 *
 * Usage:
 *   <app-skeleton variant="table" [rows]="6" [columns]="4" />
 *   <app-skeleton variant="cards" [rows]="3" />
 *   <app-skeleton variant="form"  [rows]="5" />
 *
 * For skeleton rows INSIDE an existing <tbody>, use SkeletonRowComponent
 * instead — a component host element is not valid markup between <tbody> and <tr>.
 */
@Component({
  selector: 'app-skeleton',
  standalone: true,
  imports: [CommonModule],
  template: `
    @switch (variant) {

      @case ('table') {
        <div class="sk-table" role="status" [attr.aria-label]="ariaLabel">
          <div class="sk-tr sk-head">
            @for (c of cols; track c) { <span class="sk sk-cell"></span> }
          </div>
          @for (r of items; track r) {
            <div class="sk-tr">
              @for (c of cols; track c) { <span class="sk sk-cell"></span> }
            </div>
          }
        </div>
      }

      @case ('cards') {
        <div class="row g-3" role="status" [attr.aria-label]="ariaLabel">
          @for (r of items; track r) {
            <div class="col-12 col-md-6 col-lg-4">
              <div class="sk-card">
                <span class="sk sk-line w-60 mb-2"></span>
                <span class="sk sk-line w-100 mb-1"></span>
                <span class="sk sk-line w-80 mb-3"></span>
                <span class="sk sk-pill"></span>
              </div>
            </div>
          }
        </div>
      }

      @case ('list') {
        <div role="status" [attr.aria-label]="ariaLabel">
          @for (r of items; track r) {
            <div class="sk-list-item">
              <span class="sk sk-avatar"></span>
              <span class="sk-list-text">
                <span class="sk sk-line w-40 mb-2"></span>
                <span class="sk sk-line w-70"></span>
              </span>
            </div>
          }
        </div>
      }

      @case ('form') {
        <div class="row g-3" role="status" [attr.aria-label]="ariaLabel">
          @for (r of items; track r) {
            <div class="col-12 col-md-6">
              <span class="sk sk-line w-40 mb-2"></span>
              <span class="sk sk-input"></span>
            </div>
          }
        </div>
      }

      @default {
        <div role="status" [attr.aria-label]="ariaLabel">
          @for (r of items; track r) {
            <span class="sk sk-line mb-2" [style.width.%]="textWidth(r)"></span>
          }
        </div>
      }
    }
  `,
  styles: [`
    :host { display: block; }

    .sk {
      display: block;
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

    /* Honour a reduced-motion preference: keep the placeholder, drop the sweep. */
    @media (prefers-reduced-motion: reduce) {
      .sk { animation: none; }
    }

    .sk-line   { height: 12px; width: 100%; }
    .sk-cell   { height: 12px; flex: 1 1 0; }
    .sk-pill   { height: 22px; width: 84px; border-radius: 20px; }
    .sk-input  { height: 38px; width: 100%; border-radius: 8px; }
    .sk-avatar { height: 38px; width: 38px; border-radius: 50%; flex: 0 0 auto; }

    .sk-table  { width: 100%; }
    .sk-tr {
      display: flex;
      gap: 1rem;
      padding: 0.85rem 0.25rem;
      border-bottom: 1px solid #f1f2f6;
    }
    .sk-tr.sk-head .sk-cell { height: 10px; opacity: 0.75; }
    .sk-tr:last-child { border-bottom: 0; }

    .sk-card {
      border: 1px solid #eceef4;
      border-radius: 12px;
      padding: 1rem;
    }

    .sk-list-item {
      display: flex;
      gap: 0.85rem;
      align-items: center;
      padding: 0.85rem 0.25rem;
      border-bottom: 1px solid #f1f2f6;
    }
    .sk-list-text { flex: 1 1 auto; }

    .w-40 { width: 40%; }
    .w-60 { width: 60%; }
    .w-70 { width: 70%; }
    .w-80 { width: 80%; }
  `],
})
export class SkeletonComponent {
  @Input() variant: 'text' | 'table' | 'cards' | 'list' | 'form' = 'text';
  @Input() rows = 4;
  @Input() columns = 4;
  @Input() ariaLabel = 'Loading content';

  get items(): number[] { return Array.from({ length: Math.max(1, this.rows) }, (_, i) => i); }
  get cols(): number[] { return Array.from({ length: Math.max(1, this.columns) }, (_, i) => i); }

  /** Vary the last line so a text block reads as prose rather than a solid slab. */
  textWidth(index: number): number {
    return index === this.rows - 1 ? 55 : 100;
  }
}
