import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription, interval } from 'rxjs';
import { QuestionResponseDTO, ResponseDTO, QuizSubmitRequest } from '../../../admin/models/admin-dtos';
import { AdminServiceService } from '../../../admin/services/admin-service.service';
import { UserStorageService } from '../../../auth/services/user-storage/user-storage.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-quiz-attempt',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './quiz-attempt.component.html',
  styleUrl: './quiz-attempt.component.css'
})
export class QuizAttemptComponent implements OnInit, OnDestroy {

  quizId!: number;

  questions: QuestionResponseDTO[] = [];
  responses: ResponseDTO[] = [];

  currentIndex = 0;

  isLoading = true;
  hasError = false;
  errorMessage = '';

  isSubmitting = false;
  isTester = false;

  durationSeconds = 30 * 60;
  remainingSeconds = this.durationSeconds;

  private timerSub?: Subscription;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private adminService: AdminServiceService,
    private storage: UserStorageService
  ) {}

  ngOnInit(): void {
    this.quizId = Number(this.route.snapshot.paramMap.get('quizId'));
    this.isTester = this.storage.isQuizTester();
    this.loadQuiz();
  }

  ngOnDestroy(): void {
    this.stopTimer();
  }

  loadQuiz(): void {

    this.adminService.startQuiz(this.quizId).subscribe({

      next: questions => {

        this.questions = questions ?? [];

        this.responses = this.questions.map(q => ({
          questionId: q.id,
          selectedAnswer: null
        }));

        this.startTimer();

        this.isLoading = false;

      },

      error: err => {

        console.error(err);

        this.hasError = true;
        this.errorMessage =
          err?.error?.message ??
          'Unable to start quiz.';

        this.isLoading = false;

      }

    });

  }

  private startTimer(): void {

    this.stopTimer();

    this.timerSub = interval(1000).subscribe(() => {

      if (this.remainingSeconds <= 0) {

        this.submitQuiz();

        return;

      }

      this.remainingSeconds--;

    });

  }

  private stopTimer(): void {
    this.timerSub?.unsubscribe();
  }

  selectAnswer(answer: string): void {
    this.responses[this.currentIndex].selectedAnswer = answer;
  }

  nextQuestion(): void {
    if (this.currentIndex < this.questions.length - 1) {
      this.currentIndex++;
    }
  }

  previousQuestion(): void {
    if (this.currentIndex > 0) {
      this.currentIndex--;
    }
  }

  goToQuestion(index: number): void {
    if (index >= 0 && index < this.questions.length) {
      this.currentIndex = index;
    }
  }

  submitQuiz(): void {

    if (this.isSubmitting) return;

    this.isSubmitting = true;

    const request: QuizSubmitRequest = {
      quizId: this.quizId,
      userId: 0,
      responses: this.responses
    };

    this.adminService.submitQuiz(request).subscribe({

      next: result=>{

sessionStorage.setItem(

'quiz-result',

JSON.stringify(result)

);

sessionStorage.setItem(

'quiz-submit-request',

JSON.stringify(request)

);

this.router.navigate(

['/participant/quiz/evaluation']

);

},

      error: err => {

        console.error(err);

        this.isSubmitting = false;

        alert(err?.error?.message ?? 'Failed to submit quiz.');

      }

    });

  }

  getCurrentQuestion(): QuestionResponseDTO | null {
    return this.questions[this.currentIndex] ?? null;
  }

  getSelectedAnswer(): string | null {
    return this.responses[this.currentIndex]?.selectedAnswer ?? null;
  }

  isLastQuestion(): boolean {
    return this.currentIndex === this.questions.length - 1;
  }

  isFirstQuestion(): boolean {
    return this.currentIndex === 0;
  }

  formatTime(): string {

    const minutes = Math.floor(this.remainingSeconds / 60);
    const seconds = this.remainingSeconds % 60;

    return String(minutes).padStart(2, '0') +
      ':' +
      String(seconds).padStart(2, '0');

  }


  // Option helper
  getOptions(question: QuestionResponseDTO): string[] {

  return [
    question.option1,
    question.option2,
    question.option3,
    question.option4
  ];

}

get answeredCount(): number {
  return this.responses.filter(r => r.selectedAnswer != null).length;
}

get remainingCount(): number {
  return this.questions.length - this.answeredCount;
}

exitQuiz(): void {
  if (!confirm('Exit without submitting the quiz?')) return;
  this.stopTimer();
  this.router.navigate(['/participant/quiz']); // ⚠️ verify this path — see note below
}

// goToQuestion(index: number): void {

//   if (index >= 0 && index < this.questions.length) {
//     this.currentQuestionIndex = index;
//   }

// }

// isAnswered(index: number): boolean {

//   const question = this.questions[index];

//   return !!this.answers[question.id];

// }
}