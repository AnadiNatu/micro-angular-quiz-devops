import { Routes } from '@angular/router';

export const adminRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./components/admin-dasboard/admin-dasboard.component')
        .then(m => m.AdminDasboardComponent)
  },
  {
    path: 'create-quiz',
    loadComponent: () =>
      import('./components/create-quiz/create-quiz.component')
        .then(m => m.CreateQuizComponent)
  },
  {
    path: 'create-question',
    loadComponent: () =>
      import('./components/create-question/create-question.component')
        .then(m => m.CreateQuestionComponent)
  },
  {
    path: 'quiz/view/:quizTitle',
    loadComponent: () =>
      import('./components/view-created-quiz/view-created-quiz.component')
        .then(m => m.ViewCreatedQuizComponent)
  },
  {
    path: 'creator/quizzes',
    loadComponent: () =>
      import('./components/creator-quiz-list/creator-quiz-list.component')
        .then(m => m.CreatorQuizListComponent)
  },
  {
    path: 'all/quizzes',
    loadComponent: () =>
      import('./components/creator-quiz-list/creator-quiz-list.component')
        .then(m => m.CreatorQuizListComponent)
  },
  {
    path: 'creator/quiz-detail/:quizTitle',
    loadComponent: () =>
      import('./components/creator-quiz-details/creator-quiz-details.component')
        .then(m => m.CreatorQuizDetailsComponent)
  },
  {
    // :quizId is a numeric quiz ID for the microservice
    path: 'participant/quiz/:quizId',
    loadComponent: () =>
      import('./components/quiz-attempt/quiz-attempt.component')
        .then(m => m.QuizAttemptComponent)
  },
  {
    path: 'participant/quiz',
    loadComponent: () =>
      import('./components/participant-quiz/participant-quiz.component')
        .then(m => m.ParticipantQuizComponent)
  },
  {
    path: 'participant/result',
    loadComponent: () =>
      import('./components/quiz-result/quiz-result.component')
        .then(m => m.QuizResultComponent)
  },
  {
    // :quizId is numeric quiz ID
    path: 'results/all/:quizId',
    loadComponent: () =>
      import('./components/all-user-result/all-user-result.component')
        .then(m => m.AllUserResultComponent)
  },
    {
    path: 'ai-generate-question',
    loadComponent: () =>
      import('./components/ai-generate-question/ai-generate-question.component')
        .then(m => m.AiGenerateQuestionComponent)
  },
  {
    path: 'all-questions',
    loadComponent: () =>
      import('./components/all-question/all-question.component')
        .then(m => m.AllQuestionComponent)
  },

  {
  path: 'quiz-leaderboard/:quizId',
  loadComponent: () =>
    import('./components/quiz-leaderboard/quiz-leaderboard.component')
      .then(m => m.QuizLeaderboardComponent)
},
{
  path: 'global-leaderboard',
  loadComponent: () =>
    import('./components/global-leaderboard/global-leaderboard.component')
      .then(m => m.GlobalLeaderboardComponent)
},
{
  path: 'category-leaderboard/:category',
  loadComponent: () =>
    import('./components/category-leaderboard/category-leaderboard.component')
      .then(m => m.CategoryLeaderboardComponent)
},
// {
//   // :participantId is required; :quizId is passed as a query param (?quizId=...)
//   // by quiz-leaderboard / global-leaderboard / category-leaderboard "View" actions
//   path: 'participant-ranking/:participantId',
//   loadComponent: () =>
//     import('./components/')
//       .then(m => m.ParticipantRankingComponent)
// },
{
  path: 'manage-users',
  loadComponent: () =>
    import('./components/manage-users/manage-users.component')
      .then(m => m.ManageUsersComponent)
},
{
  path: 'reports',
  loadComponent: () =>
    import('./components/admin-report-center/admin-report-center.component')
      .then(m => m.AdminReportCenterComponent)
}

];