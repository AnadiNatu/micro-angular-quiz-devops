import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth/auth.service';
import { Router, RouterLink } from '@angular/router';
import { LoginAuthRequest, UserRoles } from '../../models/dtos';
import { UserStorageService } from '../../services/user-storage/user-storage.service';
import { CommonModule } from '@angular/common';

interface DemoUser {
  name: string;
  role: string;
  username: string;
  password: string;
}

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {

  loginData: LoginAuthRequest = { username: '', password: '' };
  hovered = false;
  isLoading = false;
  errorMessage = '';

  selectedRole: 'ADMIN' | 'PARTICIPANT' | 'CURATOR' = 'PARTICIPANT';

demoUsers: Record<'ADMIN' | 'PARTICIPANT' | 'CURATOR', DemoUser[]> = {
  ADMIN: [
    {
      name: 'Administrator',
      role: 'ADMIN',
      username: 'admin',
      password: 'Admin@123'
    }
  ],

  PARTICIPANT: [
    {
      name: 'Demo Participant',
      role: 'PARTICIPANT',
      username: 'participant',
      password: 'Participant@123'
    }
  ],

  CURATOR: [
    {
      name: 'Demo Curator',
      role: 'CURATOR',
      username: 'curator',
      password: 'Curator@123'
    }
  ]

};

  constructor(
    private auth: AuthService,
    private router: Router,
    private userStorage: UserStorageService
  ) {}

  onSubmit(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.auth.login(this.loginData).subscribe({
      next: () => {
        const role = this.userStorage.getUserRole();
        switch (role) {
          case UserRoles.ADMIN:
            this.router.navigate(['/admin']);
            break;
          case UserRoles.CURATOR:
            this.router.navigate(['/admin']);
            break;
          case UserRoles.PARTICIPANT:
            this.router.navigate(['/participant']);
            break;
          default:
            this.errorMessage = 'Unknown role. Please contact support.';
        }
      },
      error: err => {
        this.isLoading = false;
        this.errorMessage = 'Login failed: ' + (err?.error || 'Invalid credentials.');
      },
      complete: () => { this.isLoading = false; }
    });
  }

  selectRole(role: 'ADMIN' | 'PARTICIPANT' | 'CURATOR'): void {
  this.selectedRole = role;
}

fillCredentials(user: DemoUser): void {
  this.loginData.username = user.username;
  this.loginData.password = user.password;
}

quickLogin(user: DemoUser): void {
  this.fillCredentials(user);
  this.onSubmit();
}
}