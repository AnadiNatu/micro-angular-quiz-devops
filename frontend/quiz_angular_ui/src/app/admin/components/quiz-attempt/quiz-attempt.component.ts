import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { AdminServiceService } from '../../services/admin-service.service';
import { QuestionResponseDTO, QuizSubmitRequest, ResponseDTO } from '../../models/admin-dtos';

@Component({
  selector: 'app-quiz-attempt',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './quiz-attempt.component.html',
  styleUrl: './quiz-attempt.component.css'
})
export class QuizAttemptComponent implements OnInit {

  quizId!: number;
  questions: QuestionResponseDTO[] = [];
  answers: Record<number, string | null> = {};
  currentQuestionIndex = 0;
  isTestMode = false;

  quizTitle = '';
category = '';
difficulty = '';

  isLoading = true;
  isSubmitting = false;
  errorMessage = '';
  submitted = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private adminService: AdminServiceService
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('quizId');
    if (!idParam) {
      this.errorMessage = 'No quiz specified.';
      this.isLoading = false;
      return;
    }
    this.quizId = Number(idParam);
    this.loadQuiz();
  }
loadQuiz(): void {

  const state = history.state;

  this.quizTitle = state?.quizTitle ?? '';
  this.category = state?.category ?? '';
  this.difficulty = state?.difficulty ?? '';

  this.isTestMode = state?.previewMode
      || this.adminService['storage'].isQuizTester();

  this.adminService
    .startQuiz(this.quizId)
    .subscribe({

      next: questions => {
        this.questions = questions;
        questions.forEach(q => { this.answers[q.id] = null; });
        this.isLoading = false;
      },

      error: err => {
        this.errorMessage = err.error?.message ?? 'Unable to load quiz';
        this.isLoading = false;
      }

    });

}


  selectAnswer(questionId: number, option: string): void {
    this.answers[questionId] = option;
  }

  submit(): void {

  const unanswered = this.remainingCount;

  if (unanswered > 0) {

    const proceed = confirm(
      `You still have ${unanswered} unanswered question(s).\n\nSubmit anyway?`
    );

    if (!proceed) {
      return;
    }

  }

  this.submitQuiz();

}

  get answeredCount(): number {

  return Object.values(this.answers)
    .filter(x => x != null)
    .length;

}

private submitQuiz(): void {

  this.isSubmitting = true;

  const responses: ResponseDTO[] = this.questions.map(q => ({
    questionId: q.id,
    selectedAnswer: this.answers[q.id]
  }));

  const request: QuizSubmitRequest = {
    quizId: this.quizId,
    userId: 0, // overwritten by AdminService
    responses
  };

  this.adminService.submitQuiz(request).subscribe({

   next: () => {

  this.isSubmitting = false;

  this.submitted = true;

  alert('✅ Quiz submitted successfully.');

  this.router.navigate(
    ['/admin/participant/result'],
    {
      state: {

        quizId: this.quizId,

        quizTitle: this.quizTitle,

        previewMode: true

      }
    }
  );

},

    error: err => {

      this.isSubmitting = false;

      this.errorMessage =
        err?.error?.message ??
        'Could not submit quiz.';

    }

  });

}

get remainingCount(): number {

  return this.questions.length - this.answeredCount;

}

get currentQuestion(): QuestionResponseDTO {

return this.questions[this.currentQuestionIndex];

}

nextQuestion(){

if(this.currentQuestionIndex<this.questions.length-1){

this.currentQuestionIndex++;

}

}

previousQuestion(){

if(this.currentQuestionIndex>0){

this.currentQuestionIndex--;

}

}

get progressPercentage(): number {

if(!this.questions.length){

return 0;

}

return (this.answeredCount/this.questions.length)*100;

}

exitQuiz(): void {

  if (!confirm('Exit without submitting the quiz?')) {
    return;
  }

  this.adminService['storage'].disableQuizTester();

  this.router.navigate(
    ['/admin/participant/quiz']
  );

}
}