import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import {
  FormBuilder, FormGroup, FormsModule,
  ReactiveFormsModule, Validators
} from '@angular/forms';
import { AdminServiceService } from '../../services/admin-service.service';
import { Router, RouterLink } from '@angular/router';
import { CreateQuestionDTO, QuestionDTO } from '../../models/admin-dtos';

@Component({
  selector: 'app-create-question',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, CommonModule, RouterLink],
  templateUrl: './create-question.component.html',
  styleUrl: './create-question.component.css'
})
export class CreateQuestionComponent implements OnInit {

  questionForm: FormGroup;
  selectedAnswer = '';
  categories: string[] = [];
  addNewCategory = false;
  submittedQuestion: QuestionDTO | null = null;

  constructor(
    private fb: FormBuilder,
    private adminService: AdminServiceService,
    public router: Router
  ) {
    this.questionForm = this.fb.group({
      questionTitle:  ['', Validators.required],
      category:       ['', Validators.required],
      difficultyLevel:['', Validators.required],
      option1:        ['', Validators.required],
      option2:        ['', Validators.required],
      option3:        ['', Validators.required],
      option4:        ['', Validators.required],
      rightAnswer:    ['', Validators.required],
      newCategory:    ['']
    });
  }

  ngOnInit(): void {
    this.adminService.getAllCategories().subscribe({
      next:  data  => this.categories = data,
      error: ()    => alert('Failed to load categories')
    });
  }

  selectRightAnswer(optionKey: string): void {
    const val = this.questionForm.get(optionKey)?.value;
    this.questionForm.patchValue({ rightAnswer: val });
    this.selectedAnswer = optionKey;
  }

  onCategoryChange(value: string): void {
    this.addNewCategory = value === 'ADD_NEW';
    if (!this.addNewCategory) {
      this.questionForm.get('newCategory')?.setValue('');
      this.questionForm.get('newCategory')?.clearValidators();
      this.questionForm.get('newCategory')?.updateValueAndValidity();
    }
  }

  submitQuestion(): void {
    if (this.questionForm.invalid) {
      this.questionForm.markAllAsTouched();
      return;
    }

    const fv = this.questionForm.value;
    const finalCategory = fv.category === 'ADD_NEW' ? fv.newCategory : fv.category;

    const dto: CreateQuestionDTO = {
      questionTitle:   fv.questionTitle,
      category:        finalCategory,
      difficultyLevel: fv.difficultyLevel,
      option1:         fv.option1,
      option2:         fv.option2,
      option3:         fv.option3,
      option4:         fv.option4,
      rightAnswer:     fv.rightAnswer
    };

    this.adminService.addQuestion(dto).subscribe({
      next: (response: QuestionDTO) => {
        this.submittedQuestion = response;
        this.questionForm.reset();
        this.addNewCategory = false;
        this.selectedAnswer = '';
      },
      error: err =>
        alert('❌ Error: ' + (err?.error?.message || 'Server Error'))
    });
  }
}