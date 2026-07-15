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
  constructor(
    private adminService: AdminServiceService,
    private storage: UserStorageService,
    private router: Router,
  ) {}
  ngOnInit() {
    this.loadCreatorQuizzes();
  }


 loadCreatorQuizzes(): void {

  this.loadingQuizzes = true;
  this.errorMessage = '';

  this.adminService.getAllQuizzesByCreator().subscribe({

    next: quizzes => {

      this.quizzes = quizzes;

      this.loadingQuizzes = false;

      if (quizzes.length > 0) {
        const incomingQuizId = (history.state as { quizId?: number })?.quizId;
        const preselect = incomingQuizId != null && quizzes.some(q => q.id === incomingQuizId)
          ? incomingQuizId
          : quizzes[0].id;
        this.onQuizChange(preselect);
      }

    },

    error: err => {

      this.loadingQuizzes = false;

      this.errorMessage =
        err?.error?.message ??
        'Unable to load quizzes.';

    }

  });

}


  onQuizChange(id: number) {
    this.selectedQuizId = +id;
    this.loadQuizStats();
    this.loadQuizResults();
  }
  loadQuizStats() {
    if (this.selectedQuizId == null) return;
    this.loadingStats = true;
    this.adminService.getQuizStats(this.selectedQuizId).subscribe({
      next: (s) => {
        this.stats = s;
        this.loadingStats = false;
      },
      error: () => (this.loadingStats = false),
    });
  }
  loadQuizResults() {
    if (this.selectedQuizId == null) return;
    this.loadingResults = true;
    this.adminService.getQuizResults(this.selectedQuizId).subscribe({
      next: (r) => {
        this.results = r;
        this.filteredResults = [...r];
        this.loadingResults = false;
      },
      error: () => (this.loadingResults = false),
    });
  }
  applySearch() {
    const t = this.searchText.trim().toLowerCase();
    this.filteredResults = !t
      ? [...this.results]
      : this.results.filter(
          (r) =>
            r.participantUsername.toLowerCase().includes(t) ||
            r.participantEmail.toLowerCase().includes(t),
        );
  }

 refresh(): void {

  if (this.selectedQuizId == null) {
    return;
  }

  this.loadQuizStats();
  this.loadQuizResults();

}

  openPrintableReport() {
    if (this.selectedQuizId == null) return;
    this.adminService.getQuizResultDocument(this.selectedQuizId).subscribe({
      next: (html) => {
        const w = window.open('', '_blank');
        if (!w) return;
        w.document.write(html);
        w.document.close();
      },
      error: () => alert('Unable to generate report.'),
    });
  }

  emailParticipant(r: QuizResultDTO) {
    this.adminService
      .sendQuizResultEmail(r.participantId, r.quizId)
      .subscribe({
        next: (m) => alert(m),
        error: () => alert('Failed to send email.'),
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

  trackResult(_: number, item: QuizResultDTO) {
    return item.id;
  }
}