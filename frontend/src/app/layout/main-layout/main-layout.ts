import { Component, AfterViewInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Header }  from '../header/header';
import { Sidebar } from '../sidebar/sidebar';
import { Footer }  from '../footer/footer';
import { MandatoryFeedbackModalComponent } from '../mandatory-feedback-modal/mandatory-feedback-modal.component';

declare const $: any;

@Component({
  selector: 'app-main-layout',
  imports: [RouterOutlet, Header, Sidebar, Footer, MandatoryFeedbackModalComponent],
  templateUrl: './main-layout.html',
})
export class MainLayout implements AfterViewInit {
  ngAfterViewInit(): void {
    setTimeout(() => $('#main-wrapper').addClass('show'), 100);
  }
}
