import { CommonModule, DecimalPipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AdminServiceService } from '../../../admin/services/admin-service.service';
import { LeaderboardEntryDTO, ParticipantRankDTO } from '../../../admin/models/admin-dtos';
import { UserStorageService } from '../../../auth/services/user-storage/user-storage.service';

@Component({
  selector: 'app-participant-ranking',
  standalone: true,
  imports: [CommonModule, DecimalPipe, RouterLink],
  templateUrl: './participant-ranking.component.html',
  styleUrl: './participant-ranking.component.css'
})
export class ParticipantRankingComponent  implements OnInit {

  participantId!: number;
  quizId!: number;

  ranking: ParticipantRankDTO | null = null;
  history: LeaderboardEntryDTO[] = [];

  isLoading = true;
  hasError = false;
  errorMessage = '';

  isAdminContext = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private adminService: AdminServiceService,
    private storage: UserStorageService
  ) {}

  ngOnInit(): void {
    // Shared by two routes: admin's '/admin/participant-ranking/:participantId'
    // (viewing someone else) and participant's '/participant/ranking' (viewing
    // their own, no route param). Prefer the route param when present.
    const routeParticipantId = this.route.snapshot.paramMap.get('participantId');
    this.isAdminContext = !!routeParticipantId;
    this.participantId = routeParticipantId
      ? Number(routeParticipantId)
      : (this.storage.getCreatorAuthUserId() ?? this.storage.getAuthUserId() ?? 0);
    this.quizId = Number(this.route.snapshot.queryParamMap.get('quizId')) || 0;
    this.loadRanking();
  }

  loadRanking(): void {

    this.isLoading = true;
    this.hasError = false;

    this.adminService.getParticipantRanking(this.participantId , this.quizId).subscribe({

      next: data => {

        this.ranking = data;
        this.history = data.quizHistory ?? [];
        this.isLoading = false;

      },

      error: err => {

        console.error(err);

        this.hasError = true;
        this.errorMessage =
          err?.error?.message ??
          'Unable to load participant ranking.';

        this.isLoading = false;

      }

    });

  }

  refresh(): void {
    this.loadRanking();
  }

  back(): void {
    if (this.isAdminContext) {
      this.router.navigate(['/admin/global-leaderboard']);
    } else {
      this.router.navigate(['/participant']);
    }
  }

  viewQuiz(quizId: number): void {
    if (this.isAdminContext) {
      this.router.navigate(['/admin/quiz-leaderboard', quizId]);
    } else {
      // No participant-facing per-quiz leaderboard page exists yet —
      // route to their own quiz history instead of the admin-only leaderboard.
      this.router.navigate(['/participant/reports']);
    }
  }

  getOverallProgress(): number {
    return Math.min(this.ranking?.averagePercentage ?? 0, 100);
  }

  getBestScore(): number {

    if (!this.history.length) {
      return 0;
    }

    return Math.max(...this.history.map(h => h.percentage));

  }

  getWorstScore(): number {

    if (!this.history.length) {
      return 0;
    }

    return Math.min(...this.history.map(h => h.percentage));

  }

  getPassRate(): number {

    if (!this.history.length) {
      return 0;
    }

    const passed = this.history.filter(h => h.percentage >= 60).length;

    return (passed * 100) / this.history.length;

  }

  getTrendClass(score: number): string {

    if (score >= 80) return 'excellent';
    if (score >= 60) return 'good';
    if (score >= 40) return 'average';

    return 'poor';

  }

  trackHistory(_: number, item: LeaderboardEntryDTO): number {
    return item.quizId;
  }

}