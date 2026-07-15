import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import {
  FormBuilder, FormGroup, FormsModule,
  ReactiveFormsModule, Validators
} from '@angular/forms';
import { AdminServiceService } from '../../services/admin-service.service';
import { Router, RouterLink } from '@angular/router';
import { CreateQuizDTO } from '../../models/admin-dtos';
import { UserStorageService } from '../../../auth/services/user-storage/user-storage.service';

@Component({
  selector: 'app-create-quiz',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, CommonModule , RouterLink],
  templateUrl: './create-quiz.component.html',
  styleUrl: './create-quiz.component.css'
})
export class CreateQuizComponent implements OnInit {

  quizForm!: FormGroup;
  categories:         string[] = [];
  questionOptions:    number[] = [];
  availableQuestions  = 0;
  maxQuestions        = 0;
  successMessage      = '';
  errorMessage        = '';

  constructor(
    private fb:           FormBuilder,
    private adminService: AdminServiceService,
    private router:       Router,
    private storage:      UserStorageService
  ) {}

  ngOnInit(): void {
    this.quizForm = this.fb.group({
      title:             ['', Validators.required],
      category:          ['', Validators.required],
      difficultyLevel:   ['', Validators.required],
      numberOfQuestions: [1, [Validators.required, Validators.min(1)]]
    });

    this.adminService.getCategories()
    .subscribe({
        next: cats => this.categories = cats,
        error: () => this.categories = []
    });
  }

  onCategoryChange(): void {
    const cat = this.quizForm.get('category')?.value;
    if (!cat) return;

    this.adminService.getQuestionCountByCategory(cat).subscribe({
      next:  n  => { this.availableQuestions = n; this.maxQuestions = n; this.buildOptions(); },
      error: () => { this.availableQuestions = 0; this.maxQuestions = 0; this.questionOptions = []; }
    });
  }

  onDifficultyChange(): void {
    const cat  = this.quizForm.get('category')?.value;
    const diff = this.quizForm.get('difficultyLevel')?.value;
    if (!cat || !diff) return;

    this.adminService.getQuestionCountByCategoryAndDifficulty(cat, diff).subscribe({
      next:  n  => { this.availableQuestions = n; this.maxQuestions = n; this.buildOptions(); },
      error: () => { this.availableQuestions = 0; this.maxQuestions = 0; this.questionOptions = []; }
    });
  }

  buildOptions(): void {
    this.questionOptions = Array.from({ length: this.maxQuestions }, (_, i) => i + 1);
  }

  setMaxQuestions(): void {
    this.quizForm.get('numberOfQuestions')?.setValue(this.maxQuestions);
  }

  createQuiz(): void {
    if (this.quizForm.invalid) return;

    const fv     = this.quizForm.value;
    const userId = this.storage.getCreatorAuthUserId();

if(userId==null){
this.errorMessage="Please login again.";
console.log(userId);

return;
}

    const dto: CreateQuizDTO = {
      title:             fv.title,
      category:          fv.category,
      difficultyLevel:   fv.difficultyLevel,
      numberOfQuestions: Number(fv.numberOfQuestions),
      createdByUserId:   userId
    };

    console.log(userId);
    console.log(dto);

    this.adminService.createQuiz(dto).subscribe({
      next: res => {
        this.successMessage = `✅ Quiz "${res.title}" created!`;
        this.errorMessage   = '';
        this.quizForm.reset();
        this.availableQuestions = 0;
        this.maxQuestions       = 0;
        this.questionOptions    = [];
        setTimeout(() =>
          this.router.navigate(['/admin/creator/quiz-detail', res.title]), 1200
        );

        console.log(dto);
      },
      error: err => {
        this.errorMessage   = '❌ ' + (err?.error?.message || 'Could not create quiz.');
        this.successMessage = '';
      }
    });
  }
}