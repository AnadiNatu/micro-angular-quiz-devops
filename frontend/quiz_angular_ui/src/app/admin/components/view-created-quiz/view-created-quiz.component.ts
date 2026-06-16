import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AdminServiceService } from '../../services/admin-service.service';
import { QuizDTO } from '../../models/admin-dtos';

@Component({
  selector: 'app-view-created-quiz',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './view-created-quiz.component.html',
  styleUrl: './view-created-quiz.component.css'
})
export class ViewCreatedQuizComponent implements OnInit {

  quizTitle       = '';
  quiz: QuizDTO | null = null;
  questionTitles: string[] = [];
  errorMessage    = '';

  constructor(
    private route:        ActivatedRoute,
    private router:       Router,
    private adminService: AdminServiceService
  ) {}

  ngOnInit(): void {
    this.quizTitle = this.route.snapshot.paramMap.get('quizTitle') || '';

    if (!this.quizTitle) {
      this.errorMessage = 'No quiz title provided.';
      return;
    }

    this.adminService.getQuizByQuizTitle(this.quizTitle).subscribe({
      next: quiz => {
        this.quiz = quiz;
        this.adminService.getQuestionTitlesOfQuiz(quiz.id).subscribe({
          next:  titles => this.questionTitles = titles,
          error: ()     => this.questionTitles = []
        });
      },
      error: err => {
        this.errorMessage = err?.error?.message || 'Quiz not found.';
      }
    });
  }

  goBack(): void { this.router.navigate(['/admin']); }
}