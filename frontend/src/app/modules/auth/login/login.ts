import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

type ForgotPasswordStep = 'email' | 'otp' | 'reset';

@Component({
  selector: 'app-login',
  imports: [FormsModule, CommonModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css'],
})
export class Login {
  email = '';
  password = '';
  errorMessage = '';
  isSubmitting = false;

  // Forgot Password modal state
  showForgotPassword = false;
  fpStep: ForgotPasswordStep = 'email';
  fpEmail = '';
  fpOtp = '';
  fpNewPassword = '';
  fpConfirmPassword = '';
  fpMessage = '';
  fpMessageType: 'success' | 'danger' = 'danger';
  fpSubmitting = false;

  constructor(private authService: AuthService, private router: Router) {}

  onLogin(): void {
    if (!this.email.trim() || !this.password.trim()) {
      this.errorMessage = 'Please enter both email and password.';
      return;
    }

    this.errorMessage = '';
    this.isSubmitting = true;

    this.authService.login(this.email.trim(), this.password).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.errorMessage = err?.error?.message || 'Login failed. Please try again.';
      },
    });
  }

  openForgotPassword(): void {
    this.showForgotPassword = true;
    this.fpStep = 'email';
    this.fpEmail = '';
    this.fpOtp = '';
    this.fpNewPassword = '';
    this.fpConfirmPassword = '';
    this.fpMessage = '';
  }

  closeForgotPassword(): void {
    this.showForgotPassword = false;
  }

  onSendOtp(): void {
    if (!this.fpEmail.trim()) {
      this.fpMessage = 'Please enter your email address';
      this.fpMessageType = 'danger';
      return;
    }

    this.fpSubmitting = true;
    this.fpMessage = '';
    this.authService.sendForgotPasswordOtp(this.fpEmail.trim()).subscribe({
      next: (res) => {
        this.fpSubmitting = false;
        this.fpMessage = res.message;
        this.fpMessageType = 'success';
        this.fpStep = 'otp';
      },
      error: (err) => {
        this.fpSubmitting = false;
        this.fpMessage = err?.error?.message || 'Failed to send OTP. Please try again';
        this.fpMessageType = 'danger';
      },
    });
  }

  onVerifyOtp(): void {
    if (!this.fpOtp.trim()) {
      this.fpMessage = 'Please enter the OTP';
      this.fpMessageType = 'danger';
      return;
    }

    this.fpSubmitting = true;
    this.fpMessage = '';
    this.authService.verifyForgotPasswordOtp(this.fpEmail.trim(), this.fpOtp.trim()).subscribe({
      next: (res) => {
        this.fpSubmitting = false;
        this.fpMessage = res.message;
        this.fpMessageType = 'success';
        this.fpStep = 'reset';
      },
      error: (err) => {
        this.fpSubmitting = false;
        this.fpMessage = err?.error?.message || 'Invalid OTP. Please try again';
        this.fpMessageType = 'danger';
      },
    });
  }

  onResetPassword(): void {
    if (!this.fpNewPassword.trim()) {
      this.fpMessage = 'Please enter new password';
      this.fpMessageType = 'danger';
      return;
    }
    if (this.fpNewPassword !== this.fpConfirmPassword) {
      this.fpMessage = 'Passwords do not match';
      this.fpMessageType = 'danger';
      return;
    }
    if (this.fpNewPassword.length < 6) {
      this.fpMessage = 'Password must be at least 6 characters long';
      this.fpMessageType = 'danger';
      return;
    }

    this.fpSubmitting = true;
    this.fpMessage = '';
    this.authService.resetForgotPassword(this.fpEmail.trim(), this.fpNewPassword, this.fpConfirmPassword).subscribe({
      next: (res) => {
        this.fpSubmitting = false;
        this.fpMessage = res.message;
        this.fpMessageType = 'success';
        setTimeout(() => this.closeForgotPassword(), 1500);
      },
      error: (err) => {
        this.fpSubmitting = false;
        this.fpMessage = err?.error?.message || 'Failed to reset password. Please try again';
        this.fpMessageType = 'danger';
      },
    });
  }
}
