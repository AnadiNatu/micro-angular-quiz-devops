import { Component, OnInit } from '@angular/core';
import { QuizResultDTO } from '../../models/admin-dtos';
import { ActivatedRoute, Router } from '@angular/router';
import { AdminServiceService } from '../../services/admin-service.service';
import { CommonModule, DecimalPipe, DatePipe } from '@angular/common';

@Component({
  selector: 'app-all-user-result',
  standalone: true,
  imports: [CommonModule, DecimalPipe, DatePipe],
  templateUrl: './all-user-result.component.html',
  styleUrl: './all-user-result.component.css'
})
export class AllUserResultComponent implements OnInit {

  quizId    = 0;
  quizTitle = '';
  results:  QuizResultDTO[] = [];
  loading   = true;
  error     = '';

  constructor(
    private route:        ActivatedRoute,
    private router:       Router,
    private adminService: AdminServiceService
  ) {}

  ngOnInit(): void {
    // Route param name kept as :quizTitle for route compatibility
    // but its value is now a numeric quiz ID
    const param = this.route.snapshot.paramMap.get('quizId')
               ?? this.route.snapshot.paramMap.get('quizTitle')
               ?? '0';

    this.quizId = Number(param);

    if (!this.quizId) {
      this.error   = 'Invalid quiz ID.';
      this.loading = false;
      return;
    }

    this.adminService.getAllUserResults(this.quizId).subscribe({
      next: res => {
        this.results   = res;
        this.quizTitle = res[0]?.quizTitle ?? `Quiz #${this.quizId}`;
        this.loading   = false;
      },
      error: () => {
        this.error   = '❌ Failed to load results.';
        this.loading = false;
      }
    });
  }

  openDocument(): void {
    this.adminService.getQuizResultDocument(this.quizId).subscribe({
      next: html => {
        const w = window.open('', '_blank');
        w?.document.write(html);
        w?.document.close();
      },
      error: () => alert('Could not open document.')
    });
  }

  goBack(): void { this.router.navigate(['/admin/creator/quizzes']); }
}