
import { CommonModule, DecimalPipe, DatePipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AdminServiceService } from '../../services/admin-service.service';
import {
  ParticipantRankDTO,
  LeaderboardEntryDTO
} from '../../models/admin-dtos';

@Component({
  selector: 'app-participant-ranking',
  standalone: true,
  imports: [CommonModule, DecimalPipe, DatePipe, RouterLink],
  templateUrl: './participant-ranking.component.html',
  styleUrl: './participant-ranking.component.css'
})
export class ParticipantRankingComponent implements OnInit {
  participantId = 0;
  quizId?: number;

  ranking: ParticipantRankDTO | null = null;
  history: LeaderboardEntryDTO[] = [];

  isLoading = true;
  hasError = false;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private adminService: AdminServiceService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.participantId = Number(params.get('participantId'));

      const queryQuizId = this.route.snapshot.queryParamMap.get('quizId');
      this.quizId = queryQuizId ? Number(queryQuizId) : undefined;

      if (!Number.isInteger(this.participantId) || this.participantId <= 0) {
        this.hasError = true;
        this.errorMessage = 'Invalid participant ID.';
        this.isLoading = false;
        return;
      }

      this.loadRanking();
    });
  }

  loadRanking(): void {
    this.isLoading = true;
    this.hasError = false;
    this.errorMessage = '';

this.adminService
  .getParticipantRanking(this.participantId, this.quizId)
  .subscribe({
    next: data => {
      this.ranking = data;
      this.history = data.quizHistory ?? [];
      this.isLoading = false;
    },
    error: err => {
      console.error('Ranking API error:', err);
      this.errorMessage = 'Unable to load participant ranking.';
      this.hasError = true;
      this.isLoading = false;
    }
  });
}

  refresh(): void {
    this.loadRanking();
  }

  back(): void {
    if (this.quizId != null) {
      this.router.navigate(['/admin/quiz-leaderboard', this.quizId]);
    } else {
      this.router.navigate(['/admin/global-leaderboard']);
    }
  }

  viewQuiz(quizId: number): void {
    this.router.navigate(['/admin/quiz-leaderboard', quizId]);
  }

  getOverallProgress(): number {
    return Math.min(this.ranking?.averagePercentage ?? 0, 100);
  }

  getBestScore(): number {
    return this.history.length
      ? Math.max(...this.history.map(h => h.percentage))
      : 0;
  }

  getWorstScore(): number {
    return this.history.length
      ? Math.min(...this.history.map(h => h.percentage))
      : 0;
  }

  getPassRate(): number {
    if (!this.history.length) return 0;

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
