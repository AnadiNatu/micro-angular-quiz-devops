import { Routes } from '@angular/router';


export const participantRoutes: Routes = [

  // Dashboard
  {
    path: '',
    loadComponent: () =>
      import('./components/participant-dashboard/participant-dashboard.component')
        .then(m => m.ParticipantDashboardComponent)
  },

  // Profile
  {
    path: 'profile',
    loadComponent: () =>
      import('./components/participant-profile/participant-profile.component')
        .then(m => m.ParticipantProfileComponent)
  },

  // Available quizzes
  {
    path: 'quiz',
    loadComponent: () =>
      import('./components/participant-quiz/participant-quiz.component')
        .then(m => m.ParticipantQuizComponent)
  },

  // Quiz evaluation — must come BEFORE 'quiz/:quizId' below, otherwise
  // Angular matches ':quizId' first and treats "evaluation" as a quiz id
  {
    path: 'quiz/evaluation',
    loadComponent: () =>
      import('./components/quiz-evaluation/quiz-evaluation.component')
        .then(m => m.QuizEvaluationComponent)
  },

  // Take quiz
  {
    path: 'quiz/:quizId',
    loadComponent: () =>
      import('./components/quiz-attempt/quiz-attempt.component')
        .then(m => m.QuizAttemptComponent)
  },

  // My ranking
  {
    path: 'ranking',
    loadComponent: () =>
      import('./components/participant-ranking/participant-ranking.component')
        .then(m => m.ParticipantRankingComponent)
  },

  // Reports
  {
    path: 'reports',
    loadComponent: () =>
      import('./components/user-report-center/user-report-center.component')
        .then(m => m.UserReportCenterComponent)
  },

  // Wildcard
  {
    path: '**',
    redirectTo: ''
  },
];