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

  ngOnInit() {
    this.userId = this.storage.getAuthUserId() ?? 0;
    this.loadResults();
  }
  loadResults() {
    this.loading = true;
    this.adminService.getResultsByUser().subscribe({
      next: (r) => {
        this.results = r;
        this.filteredResults = [...r];
        this.loading = false;
      },
      error: (e) => {
        this.loading = false;
        this.errorMessage =
          e?.error?.message ?? 'Unable to load report history.';
      },
    });
  }
  applySearch() {
    const t = this.searchText.trim().toLowerCase();
    this.filteredResults = !t
      ? [...this.results]
      : this.results.filter(
          (r) =>
            r.quizTitle.toLowerCase().includes(t) ||
            r.category.toLowerCase().includes(t),
        );
  }
  clearSearch() {
    this.searchText = '';
    this.filteredResults = [...this.results];
  }
  openReportCard() {
    this.generatingReport = true;
    this.adminService.getParticipantReportDocument(this.userId).subscribe({
      next: (html) => {
        this.generatingReport = false;
        const w = window.open('', '_blank');
        if (!w) return;
        w.document.write(html);
        w.document.close();
      },
      error: () => {
        this.generatingReport = false;
        alert('Unable to generate report card.');
      },
    });
  }
  emailReportCard() {
    this.sendingEmail = true;
    this.adminService.sendReportCardEmail(this.userId).subscribe({
      next: (m) => {
        this.sendingEmail = false;
        alert(m);
      },
      error: () => {
        this.sendingEmail = false;
        alert('Failed to email report card.');
      },
    });
  }
  printQuizResult(r: QuizResultDTO) {
    this.adminService.singleResultDocument(r.quizId, this.userId).subscribe({
      next: (html) => {
        const w = window.open('', '_blank');
        if (!w) return;
        w.document.write(html);
        w.document.close();
      },
      error: () => alert('Unable to open quiz report.'),
    });
  }
  back() {
    this.router.navigate(['/participant']);
  }
  trackResult(_: number, item: QuizResultDTO) {
    return item.id;
  }
}