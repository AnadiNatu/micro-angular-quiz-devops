import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AdminServiceService } from '../../services/admin-service.service';
import { UserStorageService } from '../../../auth/services/user-storage/user-storage.service';
import { QuizResultDTO } from '../../models/admin-dtos';
import { CommonModule, DecimalPipe, DatePipe } from '@angular/common';

@Component({
  selector: 'app-quiz-result',
  standalone: true,
  imports: [CommonModule, DecimalPipe, DatePipe],
  templateUrl: './quiz-result.component.html',
  styleUrl: './quiz-result.component.css'
})
export class QuizResultComponent implements OnInit {

  results:  QuizResultDTO[] = [];
  isLoaded  = false;
  hasError  = false;

  constructor(
    private adminService: AdminServiceService,
    private storage:      UserStorageService,
    private router:       Router
  ) {}

  ngOnInit(): void {
    this.adminService.getUserResults().subscribe({
      next: res => { this.results = res; this.isLoaded = true; },
      error: ()  => { this.hasError = true; this.isLoaded = true; }
    });
  }

  openReportCard(): void {
    const userId = this.storage.getUserId() ?? 0;
    this.adminService.getParticipantReportDocument(userId).subscribe({
      next: html => {
        const w = window.open('', '_blank');
        w?.document.write(html);
        w?.document.close();
      },
      error: () => alert('Failed to open report card.')
    });
  }

  goBack(): void { this.router.navigate(['/admin']); }
}