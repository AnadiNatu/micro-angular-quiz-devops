import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { Router } from '@angular/router';

import { AdminServiceService } from '../../services/admin-service.service';
import { UserStorageService } from '../../../auth/services/user-storage/user-storage.service';
import { QuizResultDTO } from '../../models/admin-dtos';

@Component({
  selector: 'app-quiz-result',
  standalone: true,
  imports: [
    CommonModule,
    DecimalPipe,
    DatePipe
  ],
  templateUrl: './quiz-result.component.html',
  styleUrl: './quiz-result.component.css'
})
export class QuizResultComponent implements OnInit {

  results: QuizResultDTO[] = [];

  isLoaded = false;
  hasError = false;

  testerMode = false;

  constructor(
    private adminService: AdminServiceService,
    private storage: UserStorageService,
    private router: Router
  ) {}

  ngOnInit(): void {

    this.testerMode = this.storage.isQuizTester();

    this.adminService.getResultsByUser().subscribe({

      next: (results) => {

        this.results = results;
        this.isLoaded = true;

        // Exit preview mode once results page opens
        if (this.testerMode) {
          this.storage.disableQuizTester();
        }

      },

      error: () => {

        this.hasError = true;
        this.isLoaded = true;

        if (this.testerMode) {
          this.storage.disableQuizTester();
        }

      }

    });

  }

  openReportCard(): void {

    const userId =
      this.storage.getCreatorAuthUserId()
      ?? this.storage.getAuthUserId();

    if (userId == null) {

      alert('Unable to determine current user.');

      return;

    }

    this.adminService.getParticipantReportDocument(userId).subscribe({

      next: html => {

        const popup = window.open('', '_blank');

        popup?.document.write(html);
        popup?.document.close();

      },

      error: () => {

        alert('Failed to open report card.');

      }

    });

  }

goBack(): void {

if(this.testerMode){

this.router.navigate([
'/admin/creator/quizzes'
]);

return;

}

this.router.navigate([
'/admin'
]);

}
}
