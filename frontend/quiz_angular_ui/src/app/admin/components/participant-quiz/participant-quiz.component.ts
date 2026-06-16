import { Component, OnInit, OnDestroy } from '@angular/core';
import {
  QuestionResponseDTO, QuizSubmitRequest,
  ResponseDTO, QuizResultDTO, ResultDTO,
  QuizDTO
} from '../../models/admin-dtos';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink, RouterModule } from '@angular/router';
import { AdminServiceService } from '../../services/admin-service.service';
import { UserStorageService } from '../../../auth/services/user-storage/user-storage.service';
import { CommonModule, DecimalPipe, DatePipe } from '@angular/common';

@Component({
  selector: 'app-participant-quiz',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, FormsModule, DecimalPipe, DatePipe , RouterLink],
  templateUrl: './participant-quiz.component.html',
  styleUrl: './participant-quiz.component.css'
})
export class ParticipantQuizComponent implements OnInit{
  allQuizzes:       QuizDTO[] = [];
  filteredQuizzes:  QuizDTO[] = [];
  categories:       string[]  = [];
 
  searchText        = '';
  selectedCategory  = '';
  isLoading         = true;
  errorMessage      = '';
 
  constructor(
    private adminService: AdminServiceService,
    private storage:      UserStorageService,
    private router:       Router
  ) {}
 
  ngOnInit(): void {
    this.loadQuizzes();
    this.loadCategories();
  }
 
  loadQuizzes(): void {
    this.isLoading = true;
    this.adminService.getAllQuiz().subscribe({
      next: res => {
        this.allQuizzes      = res;
        this.filteredQuizzes = res;
        this.isLoading       = false;
      },
      error: err => {
        this.errorMessage = err?.error?.message || 'Failed to load quizzes.';
        this.isLoading    = false;
      }
    });
  }
 
  loadCategories(): void {
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
 
    this.filteredQuizzes = result;
  }
 
  onCategoryChange(): void { this.applyFilters(); }
  onSearch():         void { this.applyFilters(); }
 
  clearFilters(): void {
    this.searchText       = '';
    this.selectedCategory = '';
    this.filteredQuizzes  = [...this.allQuizzes];
  }
 
  startQuiz(quiz: QuizDTO): void {
    this.router.navigate(['/admin/participant/quiz', quiz.id]);
  }
 
  getDifficultyClass(level: string): string {
    switch (level?.toUpperCase()) {
      case 'EASY':   return 'badge-easy';
      case 'MEDIUM': return 'badge-medium';
      case 'HARD':   return 'badge-hard';
      default:       return '';
    }
  }
}