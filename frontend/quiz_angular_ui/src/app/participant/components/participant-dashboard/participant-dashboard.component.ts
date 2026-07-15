import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { QuizDTO, QuizResultDTO } from '../../../admin/models/admin-dtos';
import { AdminServiceService } from '../../../admin/services/admin-service.service';
import { UsersDTO } from '../../../auth/models/dtos';
import { UserStorageService } from '../../../auth/services/user-storage/user-storage.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-participant-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './participant-dashboard.component.html',
  styleUrl: './participant-dashboard.component.css'
})
export class ParticipantDashboardComponent implements OnInit {

  isLoading = true;
  hasError = false;
  errorMessage = '';

  user: UsersDTO | null = null;
  availableQuizzes: QuizDTO[] = [];
  recentResults: QuizResultDTO[] = [];

  availableQuizCount = 0;
  totalAttempts = 0;
  averageScore = 0;
  highestScore = 0;
  latestScore = 0;

  searchText = '';
  selectedCategory = '';
  sortBy = 'latest';

  constructor(
    private adminService: AdminServiceService,
    public storage: UserStorageService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.isLoading = true;
    this.hasError = false;

    forkJoin({
      user: this.adminService.getUserDetails(),
      quizzes: this.adminService.getAllQuiz(),
      results: this.adminService.getResultsByUser()
    }).subscribe({
      next: ({user, quizzes, results}) => {
        this.user = user;
        this.availableQuizzes = quizzes ?? [];
        this.recentResults = (results ?? []).sort(
          (a,b)=> new Date(b.takenAt).getTime()-new Date(a.takenAt).getTime()
        );
        this.computeStatistics();
        this.isLoading = false;
      },
      error: (err) => {
        console.error(err);
        this.hasError = true;
        this.errorMessage = err?.error?.message ?? 'Unable to load participant dashboard.';
        this.isLoading = false;
      }
    });
  }

  refreshDashboard(): void {
    this.loadDashboard();
  }

  private computeStatistics(): void {
    this.availableQuizCount = this.availableQuizzes.length;
    this.totalAttempts = this.recentResults.length;

    if (!this.totalAttempts) {
      this.averageScore = 0;
      this.highestScore = 0;
      this.latestScore = 0;
      return;
    }

    const scores = this.recentResults.map(r => r.percentage);
    this.averageScore = scores.reduce((a,b)=>a+b,0) / scores.length;
    this.highestScore = Math.max(...scores);
    this.latestScore = this.recentResults[0].percentage;
  }

  goToAvailableQuizzes(): void {
    this.router.navigate(['/participant/quiz']);
  }

  goToResults(): void {
    this.router.navigate(['/participant/reports']);
  }

  goToProfile(): void {
    this.router.navigate(['/participant/profile']);
  }

  logout(): void {
    this.storage.logout();
    this.router.navigate(['/login']);
  }

  getDisplayName(): string {
    return this.user?.username ?? 'Participant';
  }

  getProfileImage(): string {
    return this.user?.profilePicture || 'assets/default-profile.png';
  }

  getGreeting(): string {
    const h = new Date().getHours();
    if (h < 12) return 'Good Morning';
    if (h < 17) return 'Good Afternoon';
    return 'Good Evening';
  }

}