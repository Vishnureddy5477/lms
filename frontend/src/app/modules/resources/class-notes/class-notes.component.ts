import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ResourcesService, ClassNoteItem } from '../../../services/resources.service';
import { SkeletonRowComponent } from '../../../shared/skeleton/skeleton-row.component';

@Component({
  selector: 'app-class-notes',
  standalone: true,
  imports: [CommonModule, SkeletonRowComponent],
  templateUrl: './class-notes.component.html',
  styleUrls: ['./class-notes.component.css']
})
export class ClassNotesComponent implements OnInit {
  @ViewChild('contentModal') contentModal!: ElementRef<HTMLDialogElement>;

  classNotesList: ClassNoteItem[] = [];
  loading = true;
  selectedNote: ClassNoteItem | null = null;

  constructor(private resourcesService: ResourcesService) { }

  ngOnInit(): void {
    this.resourcesService.getClassNotes().subscribe({
      next: (list) => {
        this.classNotesList = list;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  openContentModal(item: ClassNoteItem): void {
    this.selectedNote = item;
    this.contentModal.nativeElement.showModal();
  }

  closeContentModal(): void {
    this.contentModal.nativeElement.close();
    this.selectedNote = null;
  }

}
