// ─────────────────────────────────────────────────────────────────────────────
// FILE 12: src/app/admin/components/ai-generate-question/ai-generate-question.component.ts
// ─────────────────────────────────────────────────────────────────────────────
import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import {
  FormBuilder, FormGroup, FormsModule,
  ReactiveFormsModule, Validators
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AdminServiceService } from '../../services/admin-service.service';
import { UserStorageService } from '../../../auth/services/user-storage/user-storage.service';
import {
  AiGeneratedQuestionDTO,
  AiQuestionGenerateRequest,
  CreateQuestionDTO,
  QuestionDTO
} from '../../models/admin-dtos';

@Component({
  selector: 'app-ai-generate-question',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, RouterLink],
  templateUrl: './ai-generate-question.component.html',
  styleUrl:    './ai-generate-question.component.css'
})
export class AiGenerateQuestionComponent implements OnInit {

  // ── State machine ─────────────────────────────────────────────────────────
  state: 'form' | 'preview' | 'saving' | 'done' = 'form';
 
  generateForm!: FormGroup;
 
  // Categories loaded from the real backend endpoint
  categories: string[] = [];
  categoriesLoading = false;
 
  // Preview results from OpenAI
  previewQuestions: AiGeneratedQuestionDTO[] = [];
  selectedIndices   = new Set<number>();
  saveStatus: ('pending' | 'saving' | 'saved' | 'error')[] = [];
 
  isGenerating  = false;
  generateError = '';
  savedCount    = 0;
  failedCount   = 0;
 
  // Navbar state (mirrors dashboard)
  toggleMenu  = false;
  username    = '';
  userRole    = '';
 
  // Expose Math so the template can call Math.max / Math.min
  Math = Math;
 
  constructor(
    private fb:           FormBuilder,
    private adminService: AdminServiceService,
    private storage:      UserStorageService,
    public  router:       Router
  ) {}
 
  ngOnInit(): void {
    // Pull user info for the navbar
    const user    = this.storage.getUser();
    this.username = user?.username ?? '';
    this.userRole = user?.role     ?? '';
 
    this.generateForm = this.fb.group({
      topic:      ['', Validators.required],
      category:   ['', Validators.required],
      difficulty: ['', Validators.required],
      count:      [5,  [Validators.required, Validators.min(1), Validators.max(20)]]
    });
 
    // Load categories from the dedicated backend endpoint (no frontend derivation)
    this.loadCategories();
  }
 
  loadCategories(): void {
    this.categoriesLoading = true;
    this.adminService.getCategories().subscribe({
      next: cats => {
        this.categories        = cats;
        this.categoriesLoading = false;
      },
      error: () => {
        // Fallback: derive from questions list if the new endpoint isn't deployed yet
        this.adminService.getAllCategories().subscribe({
          next:  cats => { this.categories = cats; this.categoriesLoading = false; },
          error: ()   => { this.categories = [];   this.categoriesLoading = false; }
        });
      }
    });
  }
 
  // ── Step 1: generate preview ──────────────────────────────────────────────
 
  generate(): void {
    if (this.generateForm.invalid) {
      this.generateForm.markAllAsTouched();
      return;
    }
 
    this.isGenerating  = true;
    this.generateError = '';
 
    const fv = this.generateForm.value;
    const req: AiQuestionGenerateRequest = {
      topic:      fv.topic,
      category:   fv.category,
      difficulty: fv.difficulty,
      count:      Number(fv.count)
    };
 
    this.adminService.generateAiQuestions(req).subscribe({
      next: (questions) => {
        this.previewQuestions = questions;
        this.selectedIndices  = new Set(questions.map((_, i) => i));
        this.saveStatus       = questions.map(() => 'pending');
        this.isGenerating     = false;
        this.state            = 'preview';
      },
      error: (err) => {
        this.generateError = '❌ ' + (err?.error?.message || 'AI generation failed. Check your API key and try again.');
        this.isGenerating  = false;
      }
    });
  }
 
  // ── Preview helpers ───────────────────────────────────────────────────────
 
  toggleSelect(index: number): void {
    if (this.selectedIndices.has(index)) this.selectedIndices.delete(index);
    else                                  this.selectedIndices.add(index);
  }
 
  selectAll():   void { this.selectedIndices = new Set(this.previewQuestions.map((_, i) => i)); }
  deselectAll(): void { this.selectedIndices.clear(); }
 
  get selectedCount(): number { return this.selectedIndices.size; }
 
  // ── Step 2: save selected questions ──────────────────────────────────────
 
  saveSelected(): void {
    if (this.selectedIndices.size === 0) return;
 
    this.state = 'saving';
 
    const fv              = this.generateForm.value;
    const creatorId       = this.storage.getUserId()   ?? undefined;
    const creatorUsername = this.storage.getUserName() ?? undefined;
    const creatorRole     = this.storage.getUserRole() ?? undefined;
 
    const pending = [...this.selectedIndices];
    let   done    = 0;
 
    pending.forEach(index => {
      this.saveStatus[index] = 'saving';
      const q = this.previewQuestions[index];
 
      const dto: CreateQuestionDTO = {
        questionTitle:        q.question,
        category:             fv.category,
        difficultyLevel:      fv.difficulty,
        option1:              q.optionA,
        option2:              q.optionB,
        option3:              q.optionC,
        option4:              q.optionD,
        rightAnswer:          q.correctAnswer,
        creatorAuthServiceId: creatorId,
        creatorUsername,
        creatorRole
      };
 
      this.adminService.addQuestion(dto).subscribe({
        next:  () => { this.saveStatus[index] = 'saved'; this.savedCount++; done++; if (done === pending.length) this.state = 'done'; },
        error: () => { this.saveStatus[index] = 'error'; this.failedCount++; done++; if (done === pending.length) this.state = 'done'; }
      });
    });
  }
 
  // ── Restart ───────────────────────────────────────────────────────────────
 
  generateMore(): void {
    this.state            = 'form';
    this.previewQuestions = [];
    this.selectedIndices  = new Set();
    this.saveStatus       = [];
    this.savedCount       = 0;
    this.failedCount      = 0;
    this.generateError    = '';
    this.generateForm.reset({ count: 5 });
  }
 
  logout(): void {
    this.storage.logout();
    this.router.navigate(['/login']);
  }

  decreaseCount(): void {
  const current = Number(
    this.generateForm.get('count')?.value || 1
  );

  this.generateForm.get('count')?.setValue(
    Math.max(1, current - 1)
  );
}

increaseCount(): void {
  const current = Number(
    this.generateForm.get('count')?.value || 1
  );

  this.generateForm.get('count')?.setValue(
    Math.min(20, current + 1)
  );
}
}