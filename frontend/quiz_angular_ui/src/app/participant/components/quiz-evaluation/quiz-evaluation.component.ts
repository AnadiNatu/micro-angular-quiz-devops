import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { ResponseEvaluationDTO, QuizSubmitRequest } from '../../../admin/models/admin-dtos';
import { AdminServiceService } from '../../../admin/services/admin-service.service';
import { UserStorageService } from '../../../auth/services/user-storage/user-storage.service';
import { CommonModule, DecimalPipe } from '@angular/common';

@Component({
  selector: 'app-quiz-evaluation',
  standalone: true,
  imports: [CommonModule, DecimalPipe, RouterLink],
  templateUrl: './quiz-evaluation.component.html',
  styleUrl: './quiz-evaluation.component.css'
})
export class QuizEvaluationComponent implements OnInit {

  evaluations: ResponseEvaluationDTO[] = [];

  loading = true;
  hasError = false;
  errorMessage = '';

  totalQuestions = 0;
  correctAnswers = 0;
  incorrectAnswers = 0;
  percentage = 0;

  constructor(
    private adminService: AdminServiceService,
    private storage: UserStorageService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadEvaluation();
  }

  loadEvaluation(): void {

    const raw = sessionStorage.getItem('quiz-submit-request');

    if (!raw) {
      this.loading = false;
      this.hasError = true;
      this.errorMessage = 'Quiz submission data not found.';
      return;
    }

    const request: QuizSubmitRequest = JSON.parse(raw);

    this.adminService.evaluateResponses(request.responses).subscribe({

      next: (evaluation) => {

        this.evaluations = evaluation ?? [];

        this.computeSummary();

        this.loading = false;

      },

      error: (err) => {

        console.error(err);

        this.hasError = true;
        this.loading = false;
        this.errorMessage =
          err?.error?.message ??
          'Unable to evaluate quiz.';

      }

    });

  }

  private computeSummary(): void {

    this.totalQuestions = this.evaluations.length;

    this.correctAnswers = this.evaluations.filter(e =>
      e.correctAnswer?.trim().toLowerCase() ===
      e.selectedAnswer?.trim().toLowerCase()
    ).length;

    this.incorrectAnswers =
      this.totalQuestions - this.correctAnswers;

    this.percentage =
      this.totalQuestions === 0
        ? 0
        : (this.correctAnswers * 100) / this.totalQuestions;

  }

  isCorrect(item: ResponseEvaluationDTO): boolean {
    return (
      item.correctAnswer?.trim().toLowerCase() ===
      item.selectedAnswer?.trim().toLowerCase()
    );
  }

  finish(): void {

    sessionStorage.removeItem('quiz-submit-request');

    this.router.navigate(['/participant/reports']);

  }

  retakeQuiz(): void {

    history.back();

  }

  dashboard(): void {

    this.router.navigate(['/participant']);

  }

}