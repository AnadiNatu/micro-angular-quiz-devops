import { CommonModule, DecimalPipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AdminServiceService } from '../../services/admin-service.service';
import {
  GlobalLeaderboardDTO,
  GlobalRankEntryDTO,
  LeaderboardEntryDTO,
} from '../../models/admin-dtos';

@Component({
  selector: 'app-global-leaderboard',
  standalone: true,
  imports: [CommonModule, FormsModule, DecimalPipe, RouterLink],
  templateUrl: './global-leaderboard.component.html',
  styleUrl: './global-leaderboard.component.css',
})
export class GlobalLeaderboardComponent implements OnInit {
  leaderboard: GlobalLeaderboardDTO | null = null;
  rankings: GlobalRankEntryDTO[] = [];
  filteredRankings: GlobalRankEntryDTO[] = [];
  searchText = '';
  limit = 50;
  isLoading = true;
  hasError = false;
  errorMessage = '';
  constructor(
    private adminService: AdminServiceService,
    private router: Router,
  ) {}
  ngOnInit() {
    this.loadLeaderboard();
  }
  loadLeaderboard() {
    this.isLoading = true;
    this.hasError = false;
    this.adminService.getGlobalLeaderboard(this.limit).subscribe({
      next: (data) => {
        this.leaderboard = data;
        this.rankings = data.rankings ?? [];
        this.filteredRankings = [...this.rankings];
        this.isLoading = false;
      },
      error: (err) => {
        this.hasError = true;
        this.errorMessage =
          err?.error?.message ?? 'Unable to load global leaderboard.';
        this.isLoading = false;
      },
    });
  }
  refresh() {
    this.loadLeaderboard();
  }
  onSearch() {
    const t = this.searchText.trim().toLowerCase();
    this.filteredRankings = !t
      ? [...this.rankings]
      : this.rankings.filter(
          (r) =>
            r.participantUsername?.toLowerCase().includes(t) ||
            r.participantEmail?.toLowerCase().includes(t),
        );
  }
  clearSearch() {
    this.searchText = '';
    this.filteredRankings = [...this.rankings];
  }
  changeLimit(limit: number) {
    this.limit = limit;
    this.loadLeaderboard();
  }
 

  viewParticipant(
  item: GlobalRankEntryDTO
): void {

  this.router.navigate(
    [
      '/admin/participant-ranking',
      item.participantId
    ]
  );

}

  getMedal(rank: number) {
    return rank === 1 ? '🥇' : rank === 2 ? '🥈' : rank === 3 ? '🥉' : '🏅';
  }
  getProgressValue(i: GlobalRankEntryDTO) {
    return Math.min(i.averagePercentage, 100);
  }
  trackRank(_: number, i: GlobalRankEntryDTO) {
    return i.participantId;
  }
}
