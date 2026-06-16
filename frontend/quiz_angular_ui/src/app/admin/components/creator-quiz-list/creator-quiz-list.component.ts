import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { QuizDTO } from '../../models/admin-dtos';
import { AdminServiceService } from '../../services/admin-service.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-creator-quiz-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './creator-quiz-list.component.html',
  styleUrl: './creator-quiz-list.component.css'
})
export class CreatorQuizListComponent implements OnInit {

  
  quizzes:       QuizDTO[] = [];
  allQuizzes:    QuizDTO[] = [];   // master copy for client-side category filter
  errorMessage   = '';
  hoveredId      = 0;
  searchText     = '';
  showAllQuizzes = false;
 
  // Category filter
  categories:       string[] = [];
  selectedCategory  = '';
 
  @Output() viewQuiz       = new EventEmitter<QuizDTO>();
  @Output() viewAllResults = new EventEmitter<QuizDTO>();
 
  constructor(
    private adminService: AdminServiceService,
    private router: Router
  ) {}
 
  ngOnInit(): void {
    this.showAllQuizzes = this.router.url.includes('all/quizzes');
    this.loadQuizzes();
    this.loadCategories();
  }
 
  loadQuizzes(): void {
    const src$ = this.showAllQuizzes
      ? this.adminService.getAllQuiz()
      : this.adminService.getAllQuizzesByCreator();
 
    src$.subscribe({
      next:  res => { this.allQuizzes = res; this.applyFilters(); },
      error: err => this.errorMessage = err?.error?.message || 'Could not load quizzes'
    });
  }
 
  loadCategories(): void {
    // Try the dedicated quiz categories endpoint first; fall back to question categories
    this.adminService.getQuizCategories().subscribe({
      next:  cats => this.categories = cats,
      error: ()   => {
        this.adminService.getCategories().subscribe({
          next:  cats => this.categories = cats,
          error: ()   => this.categories = []
        });
      }
    });
  }
 
  // ── Filtering ───────────────────────────────────────────────────────────────
 
  applyFilters(): void {
    let result = [...this.allQuizzes];
 
    if (this.selectedCategory) {
      result = result.filter(q =>
        q.category?.toLowerCase() === this.selectedCategory.toLowerCase()
      );
    }
 
    if (this.searchText.trim()) {
      const term = this.searchText.trim().toLowerCase();
      result = result.filter(q => q.title?.toLowerCase().includes(term));
    }
 
    this.quizzes = result;
    this.errorMessage = result.length === 0 && this.allQuizzes.length > 0
      ? 'No quizzes match the selected filters.'
      : '';
  }
 
  onCategoryChange(): void {
    this.applyFilters();
  }
 
  onSearch(): void {
    if (!this.searchText.trim()) {
      this.loadQuizzes();
      return;
    }
    this.applyFilters();
  }
 
  clearFilters(): void {
    this.searchText      = '';
    this.selectedCategory = '';
    this.applyFilters();
  }
 
  // ── Navigation ───────────────────────────────────────────────────────────────
 
  viewQuizDetails(quiz: QuizDTO): void { this.router.navigate(['/admin/creator/quiz-detail', quiz.title]); }
  takeQuiz(quiz: QuizDTO):        void { this.router.navigate(['/admin/participant/quiz', quiz.id]); }
  onViewResults(quiz: QuizDTO):   void { this.viewAllResults.emit(quiz); }
}