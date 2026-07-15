import { Component, OnInit } from '@angular/core';
import { Router, RouterLink, RouterModule } from '@angular/router';
import { QuizDTO } from '../../../admin/models/admin-dtos';
import { AdminServiceService } from '../../../admin/services/admin-service.service';
import { UserStorageService } from '../../../auth/services/user-storage/user-storage.service';
import { CommonModule, DecimalPipe, DatePipe } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

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
    RouterLink,
  ],
  templateUrl: './participant-quiz.component.html',
  styleUrl: './participant-quiz.component.css',
})
export class ParticipantQuizComponent implements OnInit {
  allQuizzes: QuizDTO[] = [];
  filteredQuizzes: QuizDTO[] = [];
  categories: string[] = [];
  searchText = '';
  selectedCategory = '';
  selectedDifficulty = '';
  sortBy = 'title';
  isLoading = true;
  hasError = false;
  errorMessage = '';
  isTester = false;
  constructor(
    private adminService: AdminServiceService,
    private storage: UserStorageService,
    private router: Router,
  ) {}
  ngOnInit() {
    this.isTester =
      this.storage.isAdminLoggedIn() || this.storage.isCuratorLoggedIn();
    this.loadCategories();
    this.loadQuizzes();
  }
  refresh() {
    this.loadQuizzes();
  }
  loadQuizzes() {
    this.isLoading = true;
    this.adminService.getAllQuiz().subscribe({
      next: (q) => {
        this.allQuizzes = q ?? [];
        this.applyFilters();
        this.isLoading = false;
      },
      error: (e) => {
        this.hasError = true;
        this.errorMessage = e?.error?.message ?? 'Unable to load quizzes.';
        this.isLoading = false;
      },
    });
  }
  loadCategories() {
    this.adminService
      .getCategories()
      .subscribe({
        next: (c) => (this.categories = c ?? []),
        error: () => (this.categories = []),
      });
  }
  applyFilters() {
    let l = [...this.allQuizzes];
    if (this.selectedCategory) {
      l = l.filter(
        (q) =>
          q.category?.toLowerCase() === this.selectedCategory.toLowerCase(),
      );
    }
    if (this.selectedDifficulty) {
      l = l.filter(
        (q) =>
          q.difficultyLevel?.toUpperCase() ===
          this.selectedDifficulty.toUpperCase(),
      );
    }
    if (this.searchText.trim()) {
      const t = this.searchText.trim().toLowerCase();
      l = l.filter((q) => q.title?.toLowerCase().includes(t));
    }
    l.sort((a, b) =>
      this.sortBy === 'difficulty'
        ? a.difficultyLevel.localeCompare(b.difficultyLevel)
        : a.title.localeCompare(b.title),
    );
    this.filteredQuizzes = l;
  }
  onSearch() {
    this.applyFilters();
  }
  onCategoryChange() {
    this.applyFilters();
  }
  onDifficultyChange() {
    this.applyFilters();
  }
  clearFilters() {
    this.searchText = '';
    this.selectedCategory = '';
    this.selectedDifficulty = '';
    this.sortBy = 'title';
    this.applyFilters();
  }

  
 startQuiz(q: QuizDTO) {

    if(this.isTester){

        this.storage.enableQuizTester();

    }

    this.router.navigate(

        ['/participant/quiz', q.id],

        {

            state: {

                quizTitle: q.title,

                category: q.category,

                difficulty: q.difficultyLevel

            }

        }

    );

}
  viewQuizDetails(q: QuizDTO) {
    this.router.navigate(['/participant/quiz/details', q.id]);
  }
  getDifficultyClass(level: string) {
    switch ((level ?? '').toUpperCase()) {
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
  trackQuiz(_: number, q: QuizDTO) {
    return q.id;
  }
}
