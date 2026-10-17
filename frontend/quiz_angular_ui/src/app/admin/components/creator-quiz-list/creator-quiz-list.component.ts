import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { QuizDTO } from '../../models/admin-dtos';
import { AdminServiceService } from '../../services/admin-service.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserStorageService } from '../../../auth/services/user-storage/user-storage.service';

@Component({
  selector: 'app-creator-quiz-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './creator-quiz-list.component.html',
  styleUrl: './creator-quiz-list.component.css'
})
export class CreatorQuizListComponent implements OnInit {
 quizzes: QuizDTO[] = [];
  allQuizzes: QuizDTO[] = [];

  categories: string[] = [];
  selectedCategory = '';
  searchText = '';

  errorMessage = '';
  hoveredId: number | string | null = null;
  showAllQuizzes = false;
  isTester = false;
  isLoading = false;

  @Output() viewQuiz = new EventEmitter<QuizDTO>();
  @Output() viewAllResults = new EventEmitter<QuizDTO>();

  constructor(
    private adminService: AdminServiceService,
    private storage: UserStorageService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.showAllQuizzes = this.router.url.includes('all/quizzes');
    this.isTester = this.storage.isQuizTester();

    this.loadQuizzes();
    this.loadCategories();
  }

  loadQuizzes(): void {
    this.isLoading = true;
    this.errorMessage = '';

    const source$ = this.showAllQuizzes
      ? this.adminService.getAllQuiz()
      : this.adminService.getAllQuizzesByCreator();

    source$.subscribe({
      next: (response) => {
        this.allQuizzes = response ?? [];
        this.applyFilters();
        this.isLoading = false;
      },
      error: (err) => {
        this.allQuizzes = [];
        this.quizzes = [];
        this.errorMessage =
          err?.error?.message ?? 'Could not load quizzes.';
        this.isLoading = false;
      }
    });
  }

  loadCategories(): void {
    this.adminService.getCategories().subscribe({
      next: (categories) => {
        this.categories = [...new Set(categories ?? [])]
          .filter((category): category is string =>
            typeof category === 'string' && category.trim().length > 0
          )
          .sort((a, b) => a.localeCompare(b));
      },
      error: () => {
        // Title search and filtering by categories on loaded quizzes
        // remain available even if the categories endpoint fails.
        this.categories = [
          ...new Set(
            this.allQuizzes
              .map(quiz => quiz.category)
              .filter((category): category is string =>
                typeof category === 'string' && category.trim().length > 0
              )
          )
        ].sort((a, b) => a.localeCompare(b));
      }
    });
  }

  applyFilters(): void {
    const term = this.searchText.trim().toLocaleLowerCase();
    const category = this.selectedCategory.trim().toLocaleLowerCase();

    this.quizzes = this.allQuizzes.filter((quiz) => {
      const matchesTitle =
        !term || (quiz.title ?? '').toLocaleLowerCase().includes(term);

      const matchesCategory =
        !category ||
        (quiz.category ?? '').toLocaleLowerCase() === category;

      return matchesTitle && matchesCategory;
    });

    this.errorMessage = '';
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

  viewQuizDetails(quiz: QuizDTO): void {
    this.router.navigate([
      '/admin/creator/quiz-detail',
      quiz.title
    ]);
  }

  takeQuiz(quiz: QuizDTO): void {
    this.router.navigate([
      '/admin/participant/quiz',
      quiz.id
    ]);
  }

  onViewResults(quiz: QuizDTO): void {
    // The parent must handle this event and navigate to
    // the correct quiz-results page.
    this.viewAllResults.emit(quiz);
  }
}