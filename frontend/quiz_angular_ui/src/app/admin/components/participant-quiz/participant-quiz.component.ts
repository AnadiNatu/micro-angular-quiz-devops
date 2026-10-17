import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink, RouterModule } from '@angular/router';

import { QuizDTO, QuizResultDTO } from '../../models/admin-dtos';
import { AdminServiceService } from '../../services/admin-service.service';
import { UserStorageService } from '../../../auth/services/user-storage/user-storage.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-participant-quiz',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    FormsModule,
    DecimalPipe,
    DatePipe,
    RouterLink
  ],
  templateUrl: './participant-quiz.component.html',
  styleUrl: './participant-quiz.component.css'
})
export class ParticipantQuizComponent implements OnInit {
allQuizzes: QuizDTO[] = [];
  filteredQuizzes: QuizDTO[] = [];
  categories: string[] = [];
  userResults: QuizResultDTO[] = [];

  searchText = '';
  selectedCategory = '';

  isLoading = true;
  errorMessage = '';
  isTester = false;

  private attemptedQuizIds = new Set<number>();

  constructor(
    private adminService: AdminServiceService,
    private storage: UserStorageService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.isTester = this.storage.isQuizTester();
    this.loadQuizzesAndResults();
    this.loadCategories();
  }

  loadQuizzesAndResults(): void {
    this.isLoading = true;
    this.errorMessage = '';

    forkJoin({
      quizzes: this.adminService.getAllQuiz(),
      results: this.adminService.getResultsByUser()
    }).subscribe({
      next: ({ quizzes, results }) => {
        this.allQuizzes = quizzes ?? [];
        this.userResults = results ?? [];

        this.attemptedQuizIds = new Set(
          this.userResults.map(result => Number(result.quizId))
        );

        this.applyFilters();
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load quizzes or participant results', err);
        this.errorMessage =
          err?.error?.message ??
          'Unable to load quizzes and attempt history. Please try again.';
        this.isLoading = false;
      }
    });
  }

  loadCategories(): void {
    this.adminService.getCategories().subscribe({
      next: categories => {
        this.categories = [...new Set(categories ?? [])]
          .filter((category): category is string =>
            typeof category === 'string' && category.trim().length > 0
          )
          .sort((a, b) => a.localeCompare(b));
      },
      error: () => {
        this.categories = [
          ...new Set(
            this.allQuizzes
              .map(quiz => quiz.category)
              .filter((category): category is string =>
                !!category?.trim()
              )
          )
        ].sort((a, b) => a.localeCompare(b));
      }
    });
  }

  applyFilters(): void {
    const term = this.searchText.trim().toLowerCase();
    const category = this.selectedCategory.trim().toLowerCase();

    this.filteredQuizzes = this.allQuizzes.filter(quiz => {
      const matchesTitle =
        !term || (quiz.title ?? '').toLowerCase().includes(term);

      const matchesCategory =
        !category ||
        (quiz.category ?? '').toLowerCase() === category;

      return matchesTitle && matchesCategory;
    });
  }

  onSearch(): void {
    this.applyFilters();
  }

  onCategoryChange(): void {
    this.applyFilters();
  }

  clearFilters(): void {
    this.searchText = '';
    this.selectedCategory = '';
    this.applyFilters();
  }

  hasAttempted(quiz: QuizDTO): boolean {
    return this.attemptedQuizIds.has(Number(quiz.id));
  }

  getResultForQuiz(quiz: QuizDTO): QuizResultDTO | undefined {
    return this.userResults
      .filter(result => Number(result.quizId) === Number(quiz.id))
      .sort(
        (a, b) =>
          new Date(b.takenAt).getTime() - new Date(a.takenAt).getTime()
      )[0];
  }

  startQuiz(quiz: QuizDTO): void {
    if (this.hasAttempted(quiz)) {
      return;
    }

    // Only admin/curator preview sessions should be marked as test mode.
    if (this.isTester) {
      this.storage.enableQuizTester();
    }

    this.router.navigate(
      ['/admin/participant/quiz', quiz.id],
      {
        state: {
          previewMode: this.isTester,
          quizId: quiz.id,
          quizTitle: quiz.title,
          category: quiz.category,
          difficulty: quiz.difficultyLevel
        }
      }
    );
  }

  viewResult(quiz: QuizDTO): void {
    if (!this.hasAttempted(quiz)) {
      return;
    }

    // Opens the existing participant results page.
    this.router.navigate(['/participant/results'], {
      queryParams: { quizId: quiz.id }
    });
  }

  goToDashboard(): void {
    if (this.storage.isAdminLoggedIn()) {
      this.router.navigate(['/admin']);
    } else if (this.storage.isCuratorLoggedIn()) {
      this.router.navigate(['/curator']);
    } else {
      this.router.navigate(['/participant/dashboard']);
    }
  }

  getDifficultyClass(level: string): string {
    switch (level?.toUpperCase()) {
      case 'EASY':
        return 'badge-easy';
      case 'MEDIUM':
        return 'badge-medium';
      case 'HARD':
        return 'badge-hard';
      default:
        return '';
    }
  }

}