import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface StudyMaterialItem {
  id: number;
  moduleName: string;
  content: string;
}

export interface ClassNoteItem {
  slNo: number;
  postedDate: string;
  postedBy: string;
  batch: string;
  module: string;
  attachFile: string | null;
  content: string;
}

export interface ReferenceVideoItem {
  id: number;
  title: string;
  link: string;
  videoId: string | null;
  duration: string;
}

export interface DlModuleItem {
  moduleId: number;
  moduleName: string;
  seqNo: number;
}

export interface DlVideoNode {
  id: number;
  name: string;
  link: string;
  videoId: string | null;
}

export interface DlSubtopicNode {
  subtopicId: number;
  subtopicName: string;
  videos: DlVideoNode[];
}

export interface DlTopicNode {
  topicId: number;
  topicName: string;
  subtopics: DlSubtopicNode[];
}

export interface DlModuleContent {
  totalTopics: number;
  totalSubtopics: number;
  totalVideos: number;
  topics: DlTopicNode[];
}

@Injectable({ providedIn: 'root' })
export class ResourcesService {
  constructor(private http: HttpClient) {}

  // Study Materials
  getStudyMaterials(): Observable<StudyMaterialItem[]> {
    return this.http.get<StudyMaterialItem[]>(`${environment.apiUrl}/student/resources/study-materials`);
  }

  downloadStudyMaterial(id: number): Observable<{ link: string }> {
    return this.http.post<{ link: string }>(`${environment.apiUrl}/student/resources/study-materials/${id}/download`, {});
  }

  // Class Notes
  getClassNotes(): Observable<ClassNoteItem[]> {
    return this.http.get<ClassNoteItem[]>(`${environment.apiUrl}/student/resources/class-notes`);
  }

  // Reference Videos
  getReferenceVideoModules(): Observable<string[]> {
    return this.http.get<string[]>(`${environment.apiUrl}/student/resources/reference-videos/modules`);
  }

  getReferenceVideos(moduleName: string): Observable<ReferenceVideoItem[]> {
    return this.http.get<ReferenceVideoItem[]>(`${environment.apiUrl}/student/resources/reference-videos`, {
      params: { module: moduleName },
    });
  }

  // Digital Content
  getDigitalContentModules(): Observable<DlModuleItem[]> {
    return this.http.get<DlModuleItem[]>(`${environment.apiUrl}/student/resources/digital-content/modules`);
  }

  getDigitalContentForModule(moduleId: number): Observable<DlModuleContent> {
    return this.http.get<DlModuleContent>(`${environment.apiUrl}/student/resources/digital-content/modules/${moduleId}`);
  }
}
