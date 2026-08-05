import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { QuizResultDTO } from '../../../admin/models/admin-dtos';
import { AdminServiceService } from '../../../admin/services/admin-service.service';
import { UserStorageService } from '../../../auth/services/user-storage/user-storage.service';

@Component({
  selector: 'app-user-report-center',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './user-report-center.component.html',
  styleUrl: './user-report-center.component.css',
})
export class UserReportCenterComponent implements OnInit {
 
  userId!: number;
  results: QuizResultDTO[] = [];
  filteredResults: QuizResultDTO[] = [];
  searchText = '';
  loading = false;
  sendingEmail = false;
  generatingReport = false;
  errorMessage = '';
 
  constructor(
    private adminService: AdminServiceService,
    private storage: UserStorageService,
    private router: Router,
  ) {}
 
  ngOnInit(): void {
    // FIX 1: always use authServiceId — fallback to 0 replaced with a guard
    const id = this.storage.getAuthUserId();
    if (id == null) {
      this.errorMessage = 'Session expired. Please log in again.';
      this.router.navigate(['/login']);
      return;
    }
    this.userId = id;
    this.loadResults();
  }
 
  loadResults(): void {
    this.loading = true;
    this.adminService.getResultsByUser().subscribe({
      next: (r) => {
        this.results = r;
        this.filteredResults = [...r];
        this.loading = false;
      },
      error: (e) => {
        this.loading = false;
        this.errorMessage = e?.error?.message ?? 'Unable to load report history.';
      },
    });
  }
 
  applySearch(): void {
    const t = this.searchText.trim().toLowerCase();
    this.filteredResults = !t
      ? [...this.results]
      : this.results.filter(
          (r) =>
            r.quizTitle.toLowerCase().includes(t) ||
            r.category.toLowerCase().includes(t),
        );
  }
 
  clearSearch(): void {
    this.searchText = '';
    this.filteredResults = [...this.results];
  }
 
  openReportCard(): void {
    this.generatingReport = true;
    this.adminService.getParticipantReportDocument(this.userId).subscribe({
      next: (html) => {
        this.generatingReport = false;
        // FIX 2: guard against popup blocker
        const w = window.open('', '_blank');
        if (!w) {
          alert('Popup was blocked. Please allow popups for this site and try again.');
          return;
        }
        w.document.open();
        w.document.write(html);
        w.document.close();
      },
      error: () => {
        this.generatingReport = false;
        this.errorMessage = 'Unable to generate report card. Please try again.';
      },
    });
  }
 
  emailReportCard(): void {
    this.sendingEmail = true;
    this.adminService.sendReportCardEmail(this.userId).subscribe({
      next: (m) => {
        this.sendingEmail = false;
        // FIX 4: show the server's message (e.g. "Report card sent to user@example.com")
        alert(m || '✅ Report card sent to your email!');
      },
      error: (e) => {
        this.sendingEmail = false;
        const msg = e?.error?.message ?? e?.message ?? 'Failed to send email.';
        this.errorMessage = msg;
        alert('❌ ' + msg);
      },
    });
  }
 
  printQuizResult(r: QuizResultDTO): void {
    // FIX 3: singleResultDocument(quizId, userId) — correct argument order
    this.adminService.singleResultDocument(r.quizId, this.userId).subscribe({
      next: (html) => {
        // FIX 2: guard against popup blocker
        const w = window.open('', '_blank');
        if (!w) {
          alert('Popup was blocked. Please allow popups for this site and try again.');
          return;
        }
        w.document.open();
        w.document.write(html);
        w.document.close();
      },
      error: () => {
        this.errorMessage = 'Unable to open quiz report. Please try again.';
        alert('❌ Unable to open quiz report.');
      },
    });
  }
 
  back(): void {
    this.router.navigate(['/participant']);
  }
 
  trackResult(_: number, item: QuizResultDTO): number {
    return item.id;
  }
}