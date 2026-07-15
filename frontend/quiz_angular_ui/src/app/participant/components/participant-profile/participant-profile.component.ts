import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AdminServiceService } from '../../../admin/services/admin-service.service';
import { UsersDTO } from '../../../auth/models/dtos';
import { AuthService } from '../../../auth/services/auth/auth.service';
import { UserStorageService } from '../../../auth/services/user-storage/user-storage.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-participant-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './participant-profile.component.html',
  styleUrl: './participant-profile.component.css'
})
export class ParticipantProfileComponent implements OnInit {

  user: UsersDTO | null = null;

  isLoading = true;
  hasError = false;
  errorMessage = '';

  selectedFile: File | null = null;
  profileImage = 'assets/default-profile.png';

  constructor(
    private adminService: AdminServiceService,
    private authService: AuthService,
    private storage: UserStorageService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.isLoading = true;
    this.hasError = false;

    this.adminService.getUserDetails().subscribe({
      next: (user) => {
        this.user = user;
        if (user.profilePicture) {
          this.profileImage = user.profilePicture;
        }
        this.isLoading = false;
      },
      error: (err) => {
        console.error(err);
        this.hasError = true;
        this.errorMessage = err?.error?.message ?? 'Unable to load profile.';
        this.isLoading = false;
      }
    });
  }

  refreshProfile(): void {
    this.loadProfile();
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files?.[0] ?? null;
  }

  uploadProfilePhoto(): void {
    if (!this.selectedFile || !this.user?.username) {
      return;
    }

    this.authService
      .uploadProfilePhoto(this.user.username, this.selectedFile)
      .subscribe({
        next: (url: string) => {
          this.profileImage = url;
          if (this.user) {
            this.user.profilePicture = url;
          }
        },
        error: (err) => {
          console.error(err);
          alert('Failed to upload profile photo.');
        }
      });
  }

  editProfile(): void {
    this.router.navigate(['/participant/profile/edit']);
  }

  changePassword(): void {
    this.router.navigate(['/forgot-password']);
  }

  goDashboard(): void {
    this.router.navigate(['/participant']);
  }

  logout(): void {
    this.storage.logout();
    this.router.navigate(['/login']);
  }

  getDisplayName(): string {
    return this.user?.username ?? 'Participant';
  }

  getRole(): string {
    return this.user?.role ?? 'PARTICIPANT';
  }

  getQuestionsCreated(): number {
    return  0;
  }

  getQuizzesCreated(): number {
    return 0;
  }

  getQuizzesTaken(): number {
    return 0;
  }
}
