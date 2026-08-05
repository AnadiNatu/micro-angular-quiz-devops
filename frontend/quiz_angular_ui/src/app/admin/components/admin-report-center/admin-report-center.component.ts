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
    if (this.selectedQuizId == null) return;
    this.adminService.getQuizResultDocument(this.selectedQuizId).subscribe({
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
      error: () => alert('Unable to generate report. Please try again.'),
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