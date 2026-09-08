import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  NgZone,
  OnDestroy,
  Output,
  ViewChild,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

/**
 * Absolute URL for one of the pdf.js support files.
 *
 * These have to be absolute, not "assets/pdfjs/...". If the worker cannot be
 * loaded, pdf.js quietly falls back to importing it as a module instead, and a
 * bare relative path is not a valid module specifier — so the real problem
 * surfaces as "Failed to resolve module specifier", which says nothing useful.
 * Resolving against document.baseURI also keeps this correct when the app is
 * served under a sub-path rather than at the domain root.
 */
function asset(name: string): string {
  return new URL(`assets/pdfjs/${name}`, document.baseURI).href;
}

/**
 * A read-only PDF viewer for exam question papers.
 *
 * The legacy portal embedded Mozilla's stock viewer.html in an iframe and then
 * reached into its DOM to hide the download, print and open-file buttons. That
 * works, but it is a viewer that *can* save the file, wearing a mask. This
 * builds the viewer out of pdf.js's own components instead, with a toolbar we
 * own — so there is no download button to hide, no print pipeline to intercept
 * and no `PDFViewerApplication.download()` sitting one console command away.
 *
 * What is genuinely enforced, and what is only a deterrent:
 *
 *   enforced   the bytes are fetched with the student's bearer token from an
 *              endpoint that re-checks the test window, so the URL is useless
 *              in another tab, to another student, or after time is up
 *   enforced   the blob URL is revoked when the viewer closes
 *   deterrent  no download/print/save UI, right-click blocked, Ctrl+P / Ctrl+S
 *              swallowed, and a print stylesheet that blanks the page
 *
 * None of the deterrents survive a determined student with devtools or a
 * camera, and they are not meant to. They stop the casual save-and-share.
 */
@Component({
  selector: 'app-secure-pdf-viewer',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './secure-pdf-viewer.component.html',
  styleUrls: ['./secure-pdf-viewer.component.css'],
})
export class SecurePdfViewerComponent implements AfterViewInit, OnDestroy {

  /** API path (relative to apiUrl) that streams the PDF. */
  @Input({ required: true }) fileUrl!: string;

  /** Shown as a watermark across every page — deters photo-and-share. */
  @Input() watermark = '';

  @Output() loadFailed = new EventEmitter<string>();

  @ViewChild('container', { static: true }) containerRef!: ElementRef<HTMLDivElement>;
  @ViewChild('viewer', { static: true }) viewerRef!: ElementRef<HTMLDivElement>;

  isLoading = true;
  errorMessage = '';

  currentPage = 1;
  totalPages = 0;
  zoomPercent = 100;

  searchOpen = false;
  searchTerm = '';
  searchStatus = '';

  /** pdf.js objects, kept as `any` because the components build ships no bundled types. */
  private pdfViewer: any = null;
  private linkService: any = null;
  private eventBus: any = null;
  private findController: any = null;
  private pdfDocument: any = null;
  private blobUrl: string | null = null;
  private resizeObserver: ResizeObserver | null = null;
  private destroyed = false;

  constructor(private http: HttpClient, private zone: NgZone) {}

  ngAfterViewInit(): void {
    void this.boot();
  }

  ngOnDestroy(): void {
    this.destroyed = true;
    this.resizeObserver?.disconnect();
    this.pdfViewer?.cleanup?.();
    this.pdfViewer?.setDocument?.(null);
    this.linkService?.setDocument?.(null);
    this.pdfDocument?.destroy?.();
    this.releaseBlob();
  }

  // ─── Boot ────────────────────────────────────────────────────────

  /**
   * pdf.js is loaded lazily. It is ~1MB of parser plus a worker, and most
   * students never open a question paper in a given session — there is no
   * reason for it to sit in the main bundle.
   */
  private async boot(): Promise<void> {
    try {
      // These two imports MUST stay sequential, and the global MUST be set
      // between them. pdf_viewer.mjs is the "components" build: its very first
      // statement destructures ~50 names off globalThis.pdfjsLib at module
      // evaluation time. Load it in parallel with the library and it evaluates
      // against an undefined global — "Cannot destructure property
      // 'AbortException' of 'globalThis.pdfjsLib'".
      const pdfjs = await import('pdfjs-dist');
      if (this.destroyed) return;

      // .js, not the .mjs pdfjs-dist ships. pdf.js loads this with
      // `new Worker(src, { type: "module" })`, and a module worker is rejected
      // unless the server sends a JavaScript MIME type. Plenty of servers —
      // including the nginx in front of production — have no mapping for .mjs
      // and serve it as application/octet-stream, which the browser refuses;
      // pdf.js then falls back to importing it, which refuses the same MIME.
      // The module-ness comes from the option above, not the extension, and the
      // worker bundle has no imports of its own, so the rename is free.
      pdfjs.GlobalWorkerOptions.workerSrc = asset('pdf.worker.min.js');
      (globalThis as any).pdfjsLib = pdfjs;

      const viewerComponents: any = await import('pdfjs-dist/web/pdf_viewer.mjs');
      if (this.destroyed) return;

      const { EventBus, PDFLinkService, PDFFindController, PDFViewer } = viewerComponents;

      this.eventBus = new EventBus();
      this.linkService = new PDFLinkService({ eventBus: this.eventBus });
      this.findController = new PDFFindController({
        eventBus: this.eventBus,
        linkService: this.linkService,
      });

      this.pdfViewer = new PDFViewer({
        container: this.containerRef.nativeElement,
        viewer: this.viewerRef.nativeElement,
        eventBus: this.eventBus,
        linkService: this.linkService,
        findController: this.findController,
        // No download manager is passed at all: without one, pdf.js has nothing
        // to save a file *with*, so even an injected call has no path to disk.
        downloadManager: null,
        // -1 is AnnotationEditorType.DISABLE: not "no tool selected" but the
        // editor never wired up at all. An exam paper is not a form, and the
        // editing layer would only be another surface to police.
        annotationEditorMode: -1,
        textLayerMode: 1,
      });
      this.linkService.setViewer(this.pdfViewer);

      // pdf.js fires these from its own scroll and render loops. Routing them
      // back through the zone is what keeps the toolbar's page number and zoom
      // in step with the document rather than one interaction behind.
      const inZone = (fn: (e: any) => void) => (e: any) => this.zone.run(() => fn(e));

      this.eventBus.on('pagesinit', inZone(() => {
        this.pdfViewer.currentScaleValue = 'page-width';
        this.syncFromViewer();
      }));
      this.eventBus.on('pagechanging', inZone((e: any) => {
        this.currentPage = e.pageNumber;
      }));
      this.eventBus.on('scalechanging', inZone(() => this.syncFromViewer()));
      this.eventBus.on('updatefindmatchescount', inZone((e: any) => this.showMatches(e)));
      this.eventBus.on('updatefindcontrolstate', inZone((e: any) => this.showMatches(e)));

      await this.loadDocument(pdfjs);
    } catch (err: any) {
      this.fail(err?.message || 'The question paper could not be displayed.');
    }
  }

  /**
   * Fetch the bytes through HttpClient rather than handing pdf.js the URL.
   *
   * That is not a detail: the auth interceptor only decorates HttpClient
   * requests, so this is what puts the bearer token on the request. pdf.js then
   * renders from an in-memory blob that belongs to this tab and dies with it.
   */
  private async loadDocument(pdfjs: any): Promise<void> {
    const url = `${environment.apiUrl}${this.fileUrl}`;

    const blob = await new Promise<Blob>((resolve, reject) => {
      this.http.get(url, { responseType: 'blob' }).subscribe({ next: resolve, error: reject });
    }).catch((err: any) => {
      throw new Error(this.messageFor(err));
    });

    if (this.destroyed) return;

    this.blobUrl = URL.createObjectURL(blob);
    this.pdfDocument = await pdfjs.getDocument({
      url: this.blobUrl,
      cMapUrl: asset('cmaps/'),
      cMapPacked: true,
      standardFontDataUrl: asset('standard_fonts/'),
      wasmUrl: asset('wasm/'),
      iccUrl: asset('iccs/'),
    }).promise;

    if (this.destroyed) {
      this.pdfDocument?.destroy?.();
      return;
    }

    // Both, and in this order. The find controller sizes its page loop from
    // linkService.pagesCount, which is 0 until the link service has the
    // document — so without this line, search silently matches nothing.
    this.linkService.setDocument(this.pdfDocument);
    this.pdfViewer.setDocument(this.pdfDocument);
    this.totalPages = this.pdfDocument.numPages;
    this.isLoading = false;

    // Keep the page fitted when the window (or the modal) is resized.
    this.resizeObserver = new ResizeObserver(() => {
      if (this.pdfViewer?.currentScaleValue === 'page-width' || this.pdfViewer?.currentScaleValue === 'page-fit') {
        this.pdfViewer.currentScaleValue = this.pdfViewer.currentScaleValue;
      }
      this.pdfViewer?.update?.();
    });
    this.resizeObserver.observe(this.containerRef.nativeElement);
  }

  /**
   * Turn an HttpErrorResponse into the reason the server actually gave.
   *
   * The body is a blob here, not JSON — `responseType: 'blob'` applies to error
   * responses too — so the server's own message is not readable without
   * unpacking it. These stand in for the cases that actually occur.
   */
  private messageFor(err: any): string {
    if (err?.status === 403) {
      return 'This question paper is no longer available to you. The test window may have closed.';
    }
    if (err?.status === 404) {
      return 'This question paper could not be found. Please inform your trainer.';
    }
    if (err?.status === 502) {
      return 'The question paper could not be retrieved from storage. Please inform your trainer.';
    }
    if (err?.status === 0) {
      return 'Could not reach the server. Check your connection and try again.';
    }
    return 'The question paper could not be loaded. Please try again.';
  }

  private fail(message: string): void {
    this.isLoading = false;
    this.errorMessage = message;
    this.loadFailed.emit(message);
  }

  private syncFromViewer(): void {
    this.currentPage = this.pdfViewer?.currentPageNumber ?? 1;
    this.zoomPercent = Math.round((this.pdfViewer?.currentScale ?? 1) * 100);
  }

  private releaseBlob(): void {
    if (this.blobUrl) {
      URL.revokeObjectURL(this.blobUrl);
      this.blobUrl = null;
    }
  }

  // ─── Toolbar ─────────────────────────────────────────────────────

  previousPage(): void {
    if (this.pdfViewer && this.currentPage > 1) this.pdfViewer.currentPageNumber = this.currentPage - 1;
  }

  nextPage(): void {
    if (this.pdfViewer && this.currentPage < this.totalPages) {
      this.pdfViewer.currentPageNumber = this.currentPage + 1;
    }
  }

  goToPage(value: string): void {
    const page = Number(value);
    if (this.pdfViewer && page >= 1 && page <= this.totalPages) {
      this.pdfViewer.currentPageNumber = page;
    } else {
      this.currentPage = this.pdfViewer?.currentPageNumber ?? 1;
    }
  }

  zoomIn(): void { this.applyZoom(Math.min((this.pdfViewer?.currentScale ?? 1) * 1.1, 4)); }
  zoomOut(): void { this.applyZoom(Math.max((this.pdfViewer?.currentScale ?? 1) / 1.1, 0.25)); }
  fitWidth(): void { if (this.pdfViewer) { this.pdfViewer.currentScaleValue = 'page-width'; this.syncFromViewer(); } }

  private applyZoom(scale: number): void {
    if (!this.pdfViewer) return;
    this.pdfViewer.currentScale = scale;
    this.syncFromViewer();
  }

  toggleSearch(): void {
    this.searchOpen = !this.searchOpen;
    if (!this.searchOpen) {
      this.searchTerm = '';
      this.searchStatus = '';
      this.dispatchFind('');
    }
  }

  onSearch(): void { this.dispatchFind(this.searchTerm); }

  findNext(backwards: boolean): void {
    if (!this.searchTerm) return;
    this.eventBus?.dispatch('find', {
      source: this,
      type: 'again',
      query: this.searchTerm,
      caseSensitive: false,
      entireWord: false,
      highlightAll: true,
      findPrevious: backwards,
    });
  }

  private dispatchFind(query: string): void {
    this.eventBus?.dispatch('find', {
      source: this,
      type: '',
      query,
      caseSensitive: false,
      entireWord: false,
      highlightAll: true,
      findPrevious: false,
    });
  }

  private showMatches(e: any): void {
    if (!this.searchTerm) { this.searchStatus = ''; return; }
    const total = e?.matchesCount?.total ?? 0;
    const current = e?.matchesCount?.current ?? 0;
    this.searchStatus = total === 0 ? 'No matches' : `${current} of ${total}`;
  }

  // ─── Deterrents ──────────────────────────────────────────────────

  /** Right-click would offer "Save image as" on the rendered canvas. */
  @HostListener('contextmenu', ['$event'])
  onContextMenu(event: MouseEvent): boolean {
    event.preventDefault();
    return false;
  }

  /**
   * Ctrl/Cmd + P and Ctrl/Cmd + S, swallowed while the paper is on screen.
   *
   * Bound on window so it catches the shortcut wherever focus happens to be,
   * but declared here so it is torn down with the component — the rest of the
   * dashboard keeps its normal shortcuts.
   */
  @HostListener('window:keydown', ['$event'])
  onKeyDown(event: KeyboardEvent): void {
    const key = event.key?.toLowerCase();
    if ((event.ctrlKey || event.metaKey) && (key === 'p' || key === 's')) {
      event.preventDefault();
      event.stopPropagation();
    }
  }
}
