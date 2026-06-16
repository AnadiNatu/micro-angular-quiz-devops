import { Component } from '@angular/core';
import { SignUpRequest } from '../../models/dtos';
import { AuthService } from '../../services/auth/auth.service';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.css'
})
export class SignupComponent {

  signUpData: SignUpRequest = {
    username: '',
    password: '',
    email: '',
    role: 'PARTICIPANT'
  };

  errorMessage = '';
  successMessage = '';

  constructor(private auth: AuthService, private router: Router) {}

  selectRole(role: string): void {
    this.signUpData.role = role;
  }

  onSubmit(): void {
    this.auth.signup(this.signUpData).subscribe({
      next: () => {
        this.successMessage = 'Account created! Redirecting to login...';
        setTimeout(() => this.router.navigate(['/login']), 1500);
      },
      error: err => {
        this.errorMessage = 'Sign up failed: ' + (err?.error?.message || 'Server error.');
      }
    });
  }
}