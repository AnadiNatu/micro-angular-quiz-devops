import { CommonModule, DecimalPipe, DatePipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AdminServiceService } from '../../services/admin-service.service';
import { GlobalRankEntryDTO, LeaderboardEntryDTO, ParticipantRankDTO, QuizLeaderboardDTO } from '../../models/admin-dtos';

@Component({
  selector: 'app-quiz-leaderboard',
  standalone: true,
  imports: [CommonModule,FormsModule,DecimalPipe,DatePipe,RouterLink],
  templateUrl: './quiz-leaderboard.component.html',
  styleUrl: './quiz-leaderboard.component.css'
})
export class QuizLeaderboardComponent implements OnInit{

  quizId!:number;

 leaderboard: QuizLeaderboardDTO | null = null;

rankings: LeaderboardEntryDTO[] = [];
filteredRankings: LeaderboardEntryDTO[] = [];

searchText = '';
limit = 20;

isLoading = true;
hasError = false;
errorMessage = '';

  constructor(
    private route:ActivatedRoute,
    private router:Router,
    private adminService:AdminServiceService
  ){}

  ngOnInit():void{
    this.quizId=Number(this.route.snapshot.paramMap.get('quizId'));
    this.loadLeaderboard();
  }
loadLeaderboard(): void {

  this.isLoading = true;
  this.hasError = false;
  this.errorMessage = '';

  this.adminService.getQuizLeaderboard(this.quizId)
    .subscribe({

      next: leaderboard => {

        this.leaderboard = leaderboard;

        this.rankings =
          leaderboard.rankings ?? [];

        this.filteredRankings =
          [...this.rankings];

        this.isLoading = false;

      },

      error: err => {

        console.error(err);

        this.hasError = true;

        this.errorMessage =
          err?.error?.message ??
          'Unable to load leaderboard.';

        this.isLoading = false;

      }

    });

}

  refresh(): void {

  if (this.limit === 20) {

    this.loadLeaderboard();

  } else {

    this.loadTopLeaderboard();

  }

}


loadTopLeaderboard(): void {

  this.adminService
      .getTopForQuiz(this.quizId, this.limit)
      .subscribe({

    next: rankings => {

      this.rankings = rankings;

      this.filteredRankings = [...rankings];

    },

    error: err => {

      console.error(err);

    }

  });

}

 onSearch(): void {

  const term =
    this.searchText
      .trim()
      .toLowerCase();

  if (!term) {

    this.filteredRankings =
      [...this.rankings];

    return;

  }

  this.filteredRankings =
    this.rankings.filter(r =>

      r.participantUsername
        ?.toLowerCase()
        .includes(term)

      ||

      r.participantEmail
        ?.toLowerCase()
        .includes(term)

    );

}
  clearSearch():void{
    this.searchText='';
    this.filteredRankings=[...this.rankings];
  }

  changeLimit(limit:number):void{
    this.limit=limit;
    this.loadTopLeaderboard();
  }

viewParticipant(
  item: LeaderboardEntryDTO
): void {

  this.router.navigate(
    [
      '/admin/participant-ranking',
      item.participantId
    ],
    {
      queryParams: {
        quizId: this.quizId
      }
    }
  );

}

  back():void{
    this.router.navigate(['/admin/global-leaderboard']);
  }

  getMedal(rank:number):string{
    switch(rank){
      case 1:return '🥇';
      case 2:return '🥈';
      case 3:return '🥉';
      default:return '🏅';
    }
  }

  getProgressValue(item:LeaderboardEntryDTO):number{
    return Math.min(item.percentage,100);
  }

  trackRank(_:number,item:LeaderboardEntryDTO):number{
    return item.participantId;
  }

}
