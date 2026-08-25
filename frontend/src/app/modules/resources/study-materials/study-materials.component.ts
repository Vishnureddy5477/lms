import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ResourcesService, StudyMaterialItem } from '../../../services/resources.service';
import { SkeletonRowComponent } from '../../../shared/skeleton/skeleton-row.component';

@Component({
  selector: 'app-study-materials',
  standalone: true,
  imports: [CommonModule, SkeletonRowComponent],
  templateUrl: './study-materials.component.html',
  styleUrls: ['./study-materials.component.css']
})
export class StudyMaterialsComponent implements OnInit {

  materialsList: StudyMaterialItem[] = [];
  loading = true;

  constructor(private resourcesService: ResourcesService) { }

  ngOnInit(): void {
    this.resourcesService.getStudyMaterials().subscribe({
      next: (list) => {
        this.materialsList = list;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  onViewMaterial(item: StudyMaterialItem): void {
    window.open(item.content, '_blank');
  }

  onDownloadMaterial(item: StudyMaterialItem): void {
    this.resourcesService.downloadStudyMaterial(item.id).subscribe({
      next: (res) => window.open(res.link, '_blank'),
      error: () => alert('Failed to download material. Please try again.'),
    });
  }

}
