import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink, RouterModule } from '@angular/router';

import { QuizDTO } from '../../models/admin-dtos';
import { AdminServiceService } from '../../services/admin-service.service';
import { UserStorageService } from '../../../auth/services/user-storage/user-storage.service';

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

  searchText = '';
  selectedCategory = '';

  isLoading = true;
  errorMessage = '';

  isTester = false;

  constructor(
    private adminService: AdminServiceService,
    private storage: UserStorageService,
    private router: Router
  ) {}

  ngOnInit(): void {

    this.isTester =this.storage.isQuizTester();

    this.loadQuizzes();
    this.loadCategories();
  }

  // --------------------------------------------------
  // Load Quizzes
  // --------------------------------------------------

  loadQuizzes(): void {

    this.isLoading = true;
    this.errorMessage = '';

    this.adminService.getAllQuiz().subscribe({

      next: quizzes => {

        this.allQuizzes = quizzes;
        this.filteredQuizzes = [...quizzes];

        this.applyFilters();

        this.isLoading = false;

      },

      error: err => {

        console.error(err);

        this.errorMessage =
          err?.error?.message ??
          'Unable to load quizzes.';

        this.isLoading = false;

      }

    });

  }

  loadCategories(): void {

    this.adminService.getCategories().subscribe({

      next: categories => {

        this.categories = categories;

      },

      error: () => {

        this.categories = [];

      }

    });

  }

  // --------------------------------------------------
  // Filters
  // --------------------------------------------------

  applyFilters(): void {

    let quizzes = [...this.allQuizzes];

    if (this.selectedCategory) {

      quizzes = quizzes.filter(q =>
        q.category.toLowerCase() ===
        this.selectedCategory.toLowerCase());

    }

    if (this.searchText.trim()) {

      const term = this.searchText
        .trim()
        .toLowerCase();

      quizzes = quizzes.filter(q =>
        q.title.toLowerCase().includes(term));

    }

    this.filteredQuizzes = quizzes;

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

    this.filteredQuizzes = [...this.allQuizzes];

  }

  // --------------------------------------------------
  // Start Quiz
  // --------------------------------------------------
startQuiz(quiz: QuizDTO): void {

  this.storage.enableQuizTester();

  this.router.navigate(
    ['/admin/participant/quiz', quiz.id],
    {
      state: {

        previewMode: true,

        quizId: quiz.id,

        quizTitle: quiz.title,

        category: quiz.category,

        difficulty: quiz.difficultyLevel

      }

    }
  );

}
  // --------------------------------------------------
  // Badge Color
  // --------------------------------------------------

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