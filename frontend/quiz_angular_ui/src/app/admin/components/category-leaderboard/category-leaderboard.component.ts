import { CommonModule, DecimalPipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AdminServiceService } from '../../services/admin-service.service';
import { CategoryLeaderboardDTO, GlobalRankEntryDTO, LeaderboardEntryDTO, QuizLeaderboardDTO } from '../../models/admin-dtos';

@Component({
  selector: 'app-category-leaderboard',
  standalone: true,
  imports: [CommonModule, FormsModule, DecimalPipe, RouterLink],
  templateUrl: './category-leaderboard.component.html',
  styleUrl: './category-leaderboard.component.css'
})
export class CategoryLeaderboardComponent implements OnInit {

  category = '';

 leaderboard: CategoryLeaderboardDTO | null = null;

rankings: LeaderboardEntryDTO[] = [];

filteredRankings: LeaderboardEntryDTO[] = [];

  categories: string[] = [];

  searchText = '';

  isLoading = true;
  hasError = false;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private adminService: AdminServiceService
  ) {}

  ngOnInit(): void {

    this.category =
      this.route.snapshot.paramMap.get('category') ?? '';

    this.loadCategories();

    if (this.category) {
      this.loadLeaderboard();
    }

  }

  loadCategories(): void {

    this.adminService.getCategories().subscribe({
      next: cats => this.categories = cats ?? [],
      error: () => this.categories = []
    });

  }

  loadLeaderboard(): void {

    this.isLoading = true;
    this.hasError = false;

    this.adminService.getCategoryLeaderboard(this.category).subscribe({

      next: data => {

        this.leaderboard = data;
        this.rankings = data.rankings ?? [];
        this.filteredRankings = [...this.rankings];
        this.isLoading = false;

      },

      error: err => {

        console.error(err);

        this.hasError = true;
        this.errorMessage =
          err?.error?.message ??
          'Unable to load category leaderboard.';

        this.isLoading = false;

      }

    });

  }

  onCategoryChange(): void {

    if (!this.category) {
      this.rankings = [];
      this.filteredRankings = [];
      return;
    }

    this.loadLeaderboard();

  }

  onSearch(): void {

    const term = this.searchText.trim().toLowerCase();

    if (!term) {
      this.filteredRankings = [...this.rankings];
      return;
    }

    this.filteredRankings = this.rankings.filter(r =>
      r.participantUsername?.toLowerCase().includes(term) ||
      r.participantEmail?.toLowerCase().includes(term)
    );

  }

  clearSearch(): void {

    this.searchText = '';
    this.filteredRankings = [...this.rankings];

  }

  refresh(): void {
    this.loadLeaderboard();
  }

  viewParticipant(item: LeaderboardEntryDTO): void {
    this.router.navigate(['/admin/participant-ranking', item.participantId], {
      queryParams: { quizId: item.quizId }
    });
  }

  back(): void {
    this.router.navigate(['/admin/global-leaderboard']);
  }

  getMedal(rank: number): string {

    switch (rank) {
      case 1: return '🥇';
      case 2: return '🥈';
      case 3: return '🥉';
      default: return '🏅';
    }

  }

 getProgressValue(item: LeaderboardEntryDTO): number {

    return Math.min(item.percentage,100);

}

  trackRank(_: number, item: LeaderboardEntryDTO): number {
    return item.participantId;
  }

}