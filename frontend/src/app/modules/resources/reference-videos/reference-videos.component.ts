import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { ReferenceVideoItem, ResourcesService } from '../../../services/resources.service';
import { SkeletonComponent } from '../../../shared/skeleton/skeleton.component';

@Component({
  selector: 'app-reference-videos',
  standalone: true,
  imports: [CommonModule, SkeletonComponent],
  templateUrl: './reference-videos.component.html',
  styleUrls: ['./reference-videos.component.css']
})
export class ReferenceVideosComponent implements OnInit {

  moduleNames: string[] = [];
  loadingModules = true;

  selectedModule: string | null = null;
  videos: ReferenceVideoItem[] = [];
  loadingVideos = false;
  selectedVideo: ReferenceVideoItem | null = null;
  selectedVideoUrl: SafeResourceUrl | null = null;

  constructor(private resourcesService: ResourcesService, private sanitizer: DomSanitizer) { }

  ngOnInit(): void {
    this.resourcesService.getReferenceVideoModules().subscribe({
      next: (list) => {
        this.moduleNames = list;
        this.loadingModules = false;
      },
      error: () => {
        this.loadingModules = false;
      },
    });
  }

  onViewModule(moduleName: string): void {
    this.selectedModule = moduleName;
    this.loadingVideos = true;
    this.videos = [];
    this.selectedVideo = null;
    this.selectedVideoUrl = null;

    this.resourcesService.getReferenceVideos(moduleName).subscribe({
      next: (list) => {
        this.videos = list;
        this.loadingVideos = false;
        if (list.length > 0) {
          this.selectVideo(list[0]);
        }
      },
      error: () => {
        this.loadingVideos = false;
      },
    });
  }

  selectVideo(video: ReferenceVideoItem): void {
    this.selectedVideo = video;
    const embedUrl = video.videoId
      ? `https://www.youtube.com/embed/${video.videoId}?rel=0&modestbranding=1`
      : null;
    this.selectedVideoUrl = embedUrl ? this.sanitizer.bypassSecurityTrustResourceUrl(embedUrl) : null;
  }

  backToModules(): void {
    this.selectedModule = null;
    this.videos = [];
    this.selectedVideo = null;
    this.selectedVideoUrl = null;
  }

}
