import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AdminServiceService } from '../../services/admin-service.service';
import { FormsModule } from '@angular/forms';
import { QuizDTO, QuizStatsDTO } from '../../models/admin-dtos';
import { UserStorageService } from '../../../auth/services/user-storage/user-storage.service';

@Component({
  selector: 'app-creator-quiz-details',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './creator-quiz-details.component.html',
  styleUrl: './creator-quiz-details.component.css'
})
export class CreatorQuizDetailsComponent implements OnInit {

  quizTitle      = '';
  quiz: QuizDTO | null         = null;
  questionTitles: string[]     = [];
  stats: QuizStatsDTO | null   = null;
  errorMessage   = '';
  allQuizTitles: string[]      = [];
  selectedQuizTitle = '';
  searchText     = '';

  constructor(
    private route:        ActivatedRoute,
    private adminService: AdminServiceService,
    private router:       Router,
    private storage:      UserStorageService
  ) {}

  ngOnInit(): void {
    // Populate dropdown
    this.adminService.getAllQuizTitles().subscribe({
      next:  titles => this.allQuizTitles = titles,
      error: ()     => {} // non-critical
    });

    // Load from route param
    this.quizTitle = this.route.snapshot.paramMap.get('quizTitle') || '';
    if (this.quizTitle) this.fetchQuizDetails(this.quizTitle);
  }

  fetchQuizDetails(title: string): void {
    this.errorMessage   = '';
    this.quiz           = null;
    this.questionTitles = [];
    this.stats          = null;

    this.adminService.getQuizByQuizTitle(title).subscribe({
      next: quiz => {
        this.quiz      = quiz;
        this.quizTitle = quiz.title;

        this.adminService.getQuestionTitlesOfQuiz(quiz.id).subscribe({
          next: titles => this.questionTitles = titles,
          error: ()    => this.questionTitles = []
        });

        this.adminService.getQuizStats(quiz.id).subscribe({
          next:  stats => this.stats = stats,
          error: ()    => this.stats = null
        });
      },
      error: err => {
        this.errorMessage = err?.error?.message || 'Failed to load quiz details.';
      }
    });
  }

  onSearch(): void {
    const t = this.searchText.trim();
    if (t) this.fetchQuizDetails(t);
  }

  onDropdownSelect(title: string): void {
    this.searchText = title;
    this.fetchQuizDetails(title);
  }

  goToStats(): void {

  if (!this.quiz) {
    return;
  }

  this.router.navigate([
    '/admin/quiz-leaderboard',
    this.quiz.id
  ]);

}

 goToAllResults(): void {

  if (!this.quiz) {
    return;
  }

  this.router.navigate(
    ['/admin/results/all', this.quiz.id],
    {
      state: {
        quizTitle: this.quiz.title
      }
    }
  );

}

  takeQuiz(): void {

  if (!this.quiz) {
    return;
  }

  // Without this, quiz-result.component.ts never knows it's a preview run —
  // it shows normal participant-result copy and "Back" returns to /admin
  // instead of /admin/creator/quizzes.
  this.storage.enableQuizTester();

  this.router.navigate(
    ['/admin/participant/quiz', this.quiz.id],
    {
      state: {
        previewMode: true,
        quizTitle: this.quiz.title,
         category: this.quiz.category,
        difficulty: this.quiz.difficultyLevel,
      }
    }
  );

}

printReport(): void {

  if (!this.quiz) {
    return;
  }

  // Route through the Report Center (search/email/print) instead of an
  // instant popup, to match the rest of the admin reporting flow.
  this.router.navigate(['/admin/reports'], {
    state: {
      quizId: this.quiz.id,
      quizTitle: this.quiz.title
    }
  });

}


}