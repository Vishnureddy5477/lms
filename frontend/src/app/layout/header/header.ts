import { Component, OnInit, AfterViewInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

declare var $: any;

@Component({
  selector: 'app-header',
  imports: [RouterLink],
  templateUrl: './header.html',
  styleUrls: ['./header.css']
})
export class Header implements OnInit, AfterViewInit {

  // Typed to allow optional fields without strict compiler warnings
  currentUser: { full_name?: string; email?: string } | null = null;

  constructor(private router: Router, private authService: AuthService) {}

  ngOnInit(): void {
    const student = this.authService.getStudent();
    if (student) {
      this.currentUser = { full_name: student.name, email: student.email };
    }
  }

  ngAfterViewInit(): void {
    $('.nav-control').off('click').on('click', function () {
      $('#main-wrapper').toggleClass('menu-toggle');
      $('.hamburger').toggleClass('is-active');
    });
    if ($('.dz-theme-mode').length > 0) {
      $('.dz-theme-mode').off('click').on('click', function (this: HTMLElement) {
        $(this).toggleClass('active');
        $('body').attr('data-theme-version', $(this).hasClass('active') ? 'dark' : 'light');
      });
    }
    $('.dz-fullscreen').off('click').on('click', function () {
      const doc = document as any;
      const el = document.documentElement as any;
      if (doc.fullscreenElement || doc.webkitFullscreenElement) {
        (doc.exitFullscreen || doc.webkitExitFullscreen)?.call(doc);
      } else {
        (el.requestFullscreen || el.webkitRequestFullscreen)?.call(el);
      }
    });
  }

  navigateToHolidays(): void {
    this.router.navigate(['/holidays']);
  }

  navigateToProfile(): void {
    this.router.navigate(['/profile/personal-info']);
  }

  // Handles logout action
  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  logout(): void {
    this.onLogout();
  }
}