import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { UserStorageService } from '../../../auth/services/user-storage/user-storage.service';
import { QuizDTO, QuizResultDTO, QuizStatsDTO } from '../../models/admin-dtos';
import { AdminServiceService } from '../../services/admin-service.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-admin-report-center',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './admin-report-center.component.html',
  styleUrl: './admin-report-center.component.css',
})
export class AdminReportCenterComponent implements OnInit {
  loadingReport = false;
  quizzes: QuizDTO[] = [];
  results: QuizResultDTO[] = [];
  filteredResults: QuizResultDTO[] = [];
  stats: QuizStatsDTO | null = null;
  selectedQuizId: number | null = null;
  searchText = '';
  loadingQuizzes = false;
  loadingResults = false;
  loadingStats = false;
  errorMessage = '';
 
  private userRole = '';
 
  constructor(
    private adminService: AdminServiceService,
    private storage: UserStorageService,
    private router: Router,
  ) {}
 
  ngOnInit(): void {
    this.userRole = this.storage.getUser()?.role ?? '';
    this.loadCreatorQuizzes();
  }
 
  loadCreatorQuizzes(): void {
    this.loadingQuizzes = true;
    this.errorMessage   = '';
 
    // FIX 1: ADMIN/CURATOR load their own quizzes; PARTICIPANT loads all quizzes
    const quizzes$ = (this.userRole === 'PARTICIPANT')
      ? this.adminService.getAllQuiz()
      : this.adminService.getAllQuizzesByCreator();
 
    quizzes$.subscribe({
      next: (quizzes) => {
        this.quizzes       = quizzes;
        this.loadingQuizzes = false;
 
        if (quizzes.length > 0) {
          const incomingQuizId = (history.state as { quizId?: number })?.quizId;
          const preselect = (incomingQuizId != null && quizzes.some(q => q.id === incomingQuizId))
            ? incomingQuizId
            : quizzes[0].id;
          this.onQuizChange(preselect);
        }
      },
      error: (err) => {
        this.loadingQuizzes = false;
        this.errorMessage   = err?.error?.message ?? 'Unable to load quizzes.';
      }
    });
  }
 
  onQuizChange(id: number): void {
    this.selectedQuizId = +id;
    this.searchText     = '';
    this.loadQuizStats();
    this.loadQuizResults();
  }
 
  loadQuizStats(): void {
    if (this.selectedQuizId == null) return;
    this.loadingStats = true;
    this.adminService.getQuizStats(this.selectedQuizId).subscribe({
      next:  (s) => { this.stats = s; this.loadingStats = false; },
      error: ()  => { this.loadingStats = false; }
    });
  }
 
  loadQuizResults(): void {
    if (this.selectedQuizId == null) return;
    this.loadingResults = true;
    this.adminService.getQuizResults(this.selectedQuizId).subscribe({
      next: (r) => {
        this.results        = r;
        this.filteredResults = [...r];
        this.loadingResults  = false;
      },
      error: () => { this.loadingResults = false; }
    });
  }
 
  applySearch(): void {
    const t = this.searchText.trim().toLowerCase();
    this.filteredResults = !t
      ? [...this.results]
      : this.results.filter(
          (r) =>
            r.participantUsername.toLowerCase().includes(t) ||
            r.participantEmail.toLowerCase().includes(t)
        );
  }
 
  refresh(): void {
    if (this.selectedQuizId == null) return;
    this.loadQuizStats();
    this.loadQuizResults();
  }
 
  openPrintableReport(): void {
  if (this.selectedQuizId == null || this.loadingReport) {
    return;
  }

  this.loadingReport = true;
  this.errorMessage = '';

  this.adminService.getQuizResultDocument(this.selectedQuizId).subscribe({
    next: (html: string) => {
      this.loadingReport = false;

      if (!html || !html.trim()) {
        this.errorMessage = 'The server returned an empty report.';
        return;
      }

      const reportWindow = window.open('', '_blank');

      if (!reportWindow) {
        this.errorMessage =
          'The report could not open because the browser blocked the popup. Allow popups and try again.';
        return;
      }

      reportWindow.document.open();
      reportWindow.document.write(html);
      reportWindow.document.close();
      reportWindow.focus();
    },

    error: (err) => {
      this.loadingReport = false;

      if (err.status === 401) {
        this.errorMessage =
          'Your session is not authorized. Please sign in again if your session has expired.';
      } else if (err.status === 403) {
        this.errorMessage =
          'Access denied. This report requires an ADMIN or CURATOR authority. Verify your account role and backend security configuration.';
      } else {
        this.errorMessage =
          err?.error?.message ||
          `Unable to generate the report (HTTP ${err.status || 'unknown'}). Check the Notification Service logs.`;
      }

      console.error('Quiz report generation failed:', {
        status: err.status,
        message: err.message,
        error: err.error
      });
    }
  });
}
 
  emailParticipant(r: QuizResultDTO): void {
    this.adminService.sendQuizResultEmail(r.participantId, r.quizId).subscribe({
      // FIX 4: show server confirmation message
      next:  (m) => alert(m || `✅ Email sent to ${r.participantEmail}`),
      error: ()  => alert('❌ Failed to send email. Please try again.'),
    });
  }
 
  getPassRate(): number {
    if (!this.stats) return 0;
    const total = this.stats.passCount + this.stats.failCount;
    return total ? (this.stats.passCount * 100) / total : 0;
  }
 
  back(): void {
    this.router.navigate(['/admin']);
  }
 
  trackResult(_: number, item: QuizResultDTO): number {
    return item.id;
  }
}