import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { DlModuleContent, DlModuleItem, DlVideoNode, ResourcesService } from '../../../services/resources.service';
import { SkeletonComponent } from '../../../shared/skeleton/skeleton.component';

@Component({
  selector: 'app-digital-content',
  standalone: true,
  imports: [CommonModule, SkeletonComponent],
  templateUrl: './digital-content.component.html',
  styleUrls: ['./digital-content.component.css']
})
export class DigitalContentComponent implements OnInit {

  modulesList: DlModuleItem[] = [];
  loadingModules = true;

  selectedModuleId: number | null = null;
  moduleContent: DlModuleContent | null = null;
  isLoading = false;

  activeVideo: DlVideoNode | null = null;
  activeVideoUrl: SafeResourceUrl | null = null;

  constructor(private resourcesService: ResourcesService, private sanitizer: DomSanitizer) { }

  ngOnInit(): void {
    this.resourcesService.getDigitalContentModules().subscribe({
      next: (list) => {
        this.modulesList = list;
        this.loadingModules = false;
        if (list.length > 0) {
          this.selectModule(list[0].moduleId);
        }
      },
      error: () => {
        this.loadingModules = false;
      },
    });
  }

  selectModule(id: number): void {
    this.selectedModuleId = id;
    this.isLoading = true;
    this.moduleContent = null;
    this.closeVideo();

    this.resourcesService.getDigitalContentForModule(id).subscribe({
      next: (content) => {
        this.moduleContent = content;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  get selectedModuleName(): string {
    return this.modulesList.find(m => m.moduleId === this.selectedModuleId)?.moduleName || '';
  }

  playVideo(video: DlVideoNode): void {
    this.activeVideo = video;
    const embedUrl = video.videoId
      ? `https://www.youtube.com/embed/${video.videoId}?autoplay=1`
      : null;
    this.activeVideoUrl = embedUrl ? this.sanitizer.bypassSecurityTrustResourceUrl(embedUrl) : null;
  }

  closeVideo(): void {
    this.activeVideo = null;
    this.activeVideoUrl = null;
  }

}
