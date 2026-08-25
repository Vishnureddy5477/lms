import { Component, AfterViewInit } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

declare var $: any;

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.html',
})
export class Sidebar implements AfterViewInit {
  ngAfterViewInit(): void {
    if ($('#menu').length > 0 && typeof $.fn.metisMenu === 'function') {
      $('#menu').metisMenu();
    }
  }
}
