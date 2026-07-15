import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AdminServiceService } from '../../services/admin-service.service';
import { QuestionDTO } from '../../models/admin-dtos';

@Component({
  selector: 'app-all-question',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink
  ],
  templateUrl: './all-question.component.html',
  styleUrl: './all-question.component.css'
})
export class AllQuestionComponent implements OnInit {

  // -----------------------------------------------------
  // Master Data
  // -----------------------------------------------------

  questions: QuestionDTO[] = [];

  filteredQuestions: QuestionDTO[] = [];

  categories: string[] = [];

  difficultyLevels: string[] = [
    'EASY',
    'MEDIUM',
    'HARD'
  ];

  // -----------------------------------------------------
  // Filters
  // -----------------------------------------------------

  searchText = '';

  selectedCategory = '';

  selectedDifficulty = '';

  // -----------------------------------------------------
  // UI
  // -----------------------------------------------------

  loading = false;

  deletingQuestionId: number | null = null;

  constructor(
    private adminService: AdminServiceService,
    public router: Router
  ) {}

  // -----------------------------------------------------
  // Lifecycle
  // -----------------------------------------------------

  ngOnInit(): void {

    this.loadCategories();

    this.loadQuestions();

  }

  // -----------------------------------------------------
  // Load Categories
  // -----------------------------------------------------

  loadCategories(): void {

    this.adminService.getCategories().subscribe({

      next: (categories) => {

        this.categories = categories;

      },

      error: () => {

        alert('Unable to load categories.');

      }

    });

  }

  // -----------------------------------------------------
  // Load Questions
  // -----------------------------------------------------

  loadQuestions(): void {

    this.loading = true;

    this.adminService.getQuestionsByCreator().subscribe({

      next: (questions) => {

        this.questions = questions;

        this.filteredQuestions = [...questions];

        this.loading = false;

      },

      error: (err) => {

        console.error(err);

        this.loading = false;

        alert('Unable to load your questions.');

      }

    });

  }

  // -----------------------------------------------------
  // Search / Filters
  // -----------------------------------------------------

  applyFilters(): void {

    let result = [...this.questions];

    // Search Question Title

    if (this.searchText.trim()) {

      const search = this.searchText.toLowerCase();

      result = result.filter(question =>

        question.questionTitle
          .toLowerCase()
          .includes(search)

      );

    }

    // Category Filter

    if (this.selectedCategory) {

      result = result.filter(question =>

        question.category === this.selectedCategory

      );

    }

    // Difficulty Filter

    if (this.selectedDifficulty) {

      result = result.filter(question =>

        question.difficultyLevel === this.selectedDifficulty

      );

    }

    this.filteredQuestions = result;

  }

  // -----------------------------------------------------
  // Reset Filters
  // -----------------------------------------------------

  clearFilters(): void {

    this.searchText = '';

    this.selectedCategory = '';

    this.selectedDifficulty = '';

    this.filteredQuestions = [...this.questions];

  }

  // -----------------------------------------------------
  // Delete
  // -----------------------------------------------------

  deleteQuestion(question: QuestionDTO): void {

    const confirmed = confirm(

      `Delete "${question.questionTitle}" ?`

    );

    if (!confirmed) {

      return;

    }

    this.deletingQuestionId = question.id;

    this.adminService.deleteQuestion(question.id).subscribe({

      next: () => {

        this.questions = this.questions.filter(

          q => q.id !== question.id

        );

        this.applyFilters();

        this.deletingQuestionId = null;

      },

      error: err => {

        console.error(err);

        this.deletingQuestionId = null;

        alert(

          err?.error?.message ??

          'Unable to delete question.'

        );

      }

    });

  }

  // -----------------------------------------------------
  // View Question
  // -----------------------------------------------------

  viewQuestion(question: QuestionDTO): void {

    this.router.navigate([

      '/admin/question',

      question.id

    ]);

  }

  // -----------------------------------------------------
  // Difficulty Badge
  // -----------------------------------------------------

  getDifficultyClass(level: string): string {

    switch (level) {

      case 'EASY':
        return 'difficulty-easy';

      case 'MEDIUM':
        return 'difficulty-medium';

      case 'HARD':
        return 'difficulty-hard';

      default:
        return '';

    }
  }
}