import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router, RouterLink, RouterModule, RouterOutlet } from '@angular/router';
import { AdminServiceService } from '../../services/admin-service.service';
import { CreatorQuizListComponent } from '../creator-quiz-list/creator-quiz-list.component';
import { AuthService } from '../../../auth/services/auth/auth.service';
import { UserStorageService } from '../../../auth/services/user-storage/user-storage.service';
import { UsersDTO } from '../../../auth/models/dtos';
import { QuizDTO } from '../../models/admin-dtos';

@Component({
  selector: 'app-admin-dasboard',
  standalone: true,
  imports: [RouterLink, CommonModule, CreatorQuizListComponent, RouterOutlet, RouterModule],
  templateUrl: './admin-dasboard.component.html',
  styleUrl: './admin-dasboard.component.css'
})
export class AdminDasboardComponent implements OnInit {

  takenQuizTitles: string[] = [];
  username    = '';
  userRole    = '';
  showAdminCard  = false;
  profileImageUrl = 'assets/default-profile.png';
  userDetails: UsersDTO | null = null;
  toggleMenu  = false;
  createdQuizzes: QuizDTO[] = [];

  // ── Mock stats for demo purposes (overridden by real data if available) ──
  mockStats = {
    totalQuizzes:   12,
    totalQuestions: 84,
    participants:   37
  };

  constructor(
    private adminService: AdminServiceService,
    private authService:  AuthService,
    public  storage:      UserStorageService,
    private router:       Router
  ) {}

  ngOnInit(): void {
    const user = this.storage.getUser();
    this.username = user?.username ?? 'User';
    this.userRole = user?.role     ?? '';

    if (this.userRole === 'PARTICIPANT') {
      this.adminService.getAllTakenQuizTitles().subscribe({
        next: titles => this.takenQuizTitles = titles,
        error: err   => console.warn('Could not load taken quizzes:', err)
      });
    }

    this.adminService.getUserDetails().subscribe({
      next: res => {
        this.userDetails = res;
        if (res.profilePicture) {
          this.profileImageUrl = res.profilePicture;
        }
      },
      error: err => console.warn('Could not load user details:', err)
    });

    // Try to load real stats for display
    this.adminService.getAllQuizzesByCreator().subscribe({
       next: quizzes => {

    this.createdQuizzes = quizzes;

    this.mockStats.totalQuizzes = quizzes.length;

  },

  error: err => {

    console.error(err);

  }

    });
  }

  uploadProfilePhoto(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file  = input.files?.[0];
    if (file && this.username) {
      this.authService.uploadProfilePhoto(this.username, file).subscribe({
        next: url  => this.profileImageUrl = url,
        error: err => console.error('Upload failed:', err)
      });
    }
  }

  // viewCreatedQuiz(quiz: QuizDTO): void {
  //   this.router.navigate(['/admin/quiz/view', quiz.title]);
  // }

 viewAllResultsForQuiz(quiz: QuizDTO): void {

  this.router.navigate(
    ['/admin/results/all', quiz.id],
    {
      state: {
        quizTitle: quiz.title
      }
    }
  );

}
  viewParticipantResult(): void {
    this.router.navigate(['/admin/participant/result']);
  }

  toggleAdminCard(): void {
    this.showAdminCard = !this.showAdminCard;
  }

  logout(): void {
    this.storage.logout();
    this.router.navigate(['/login']);
  }

  openQuizPreview(): void {

  this.router.navigate([
    '/admin/participant/quiz'
  ]);

}

openLeaderboards(): void {

  this.router.navigate([
    '/admin/global-leaderboard'
  ]);

}

viewCreatedQuiz(quiz: QuizDTO): void {

  this.router.navigate(
    ['/admin/quiz/view', quiz.title],
    {
      state: {
        quizId: quiz.id,
        quizTitle: quiz.title
      }
    }
  );

}

// Inline SVG avatar so a missing/unset profile photo never 404s or
// shows broken-image alt text — no dependency on an actual asset file.
private fallbackAvatar =
  'data:image/svg+xml;utf8,' + encodeURIComponent(`
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 96 96">
      <rect width="96" height="96" rx="48" fill="#dbeafe"/>
      <circle cx="48" cy="38" r="16" fill="#60a5fa"/>
      <path d="M20 82c4-18 20-26 28-26s24 8 28 26" fill="#60a5fa"/>
    </svg>
  `);

onAvatarError(event: Event): void {
  const img = event.target as HTMLImageElement;
  img.onerror = null; // prevent any retry loop if the fallback itself ever failed
  img.src = this.fallbackAvatar;
}


}