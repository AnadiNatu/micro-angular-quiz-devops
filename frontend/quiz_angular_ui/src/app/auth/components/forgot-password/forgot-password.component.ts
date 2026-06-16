import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth/auth.service';
import { ForgotPasswordRequest } from '../../models/dtos';
import { Router } from '@angular/router';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.css'
})
export class ForgotPasswordComponent {

  userEmail = '';
  successMessage = '';
  errorMessage = '';

  constructor(private authService: AuthService, private router: Router) {}

  onSendResetLink(): void {
    const payload: ForgotPasswordRequest = { email: this.userEmail };

    this.authService.forgotPassword(payload).subscribe({
      next: () => {
        this.successMessage = 'Reset link sent! Check your email.';
        this.errorMessage = '';
        setTimeout(() => {
          this.router.navigate(['/reset-password'], {
            queryParams: { email: this.userEmail }
          });
        }, 1500);
      },
      error: err => {
        this.errorMessage = err?.error || 'Failed to send reset token.';
        this.successMessage = '';
      }
    });
  }
}