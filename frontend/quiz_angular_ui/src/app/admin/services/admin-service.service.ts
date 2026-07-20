import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import {
  CreateQuestionDTO, QuestionDTO, QuestionResponseDTO,
  CreateQuizDTO, QuizDTO,
  QuizSubmitRequest, ResultDTO,
  QuizResultDTO, QuizStatsDTO,
  AiGeneratedQuestionDTO,
  AiQuestionGenerateRequest,
  ResponseDTO,
  ResponseEvaluationDTO,
  QuizLeaderboardDTO,
  LeaderboardEntryDTO,
  GlobalLeaderboardDTO,
  CategoryLeaderboardDTO,
  ParticipantRankDTO
} from '../models/admin-dtos';
import { UsersDTO } from '../../auth/models/dtos';
import { UserStorageService } from '../../auth/services/user-storage/user-storage.service';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AdminServiceService {

  private readonly BASE = environment.apiBaseUrl;

  private AUTH     = `${this.BASE}/api/auth`;
  private QUESTION = `${this.BASE}/api/questions`;
  private QUIZ     = `${this.BASE}/api/quiz`;
  private NOTIFY   = `${this.BASE}/api/notify`;
  private LEADERSHIP = `${this.BASE}/api/leaderboard`;

  constructor(
    private http: HttpClient,
    private storage: UserStorageService
  ) {}

  // ── Helpers ──────────────────────────────────────────────

  private h(): HttpHeaders {
    return new HttpHeaders({ Authorization: `Bearer ${this.storage.getToken()}` });
  }

  private jh(): HttpHeaders {
    return new HttpHeaders({
      Authorization:  `Bearer ${this.storage.getToken()}`,
      'Content-Type': 'application/json'
    });
  }

  // ── Auth / User ───────────────────────────────────────────

  getUserDetails(): Observable<UsersDTO> {
    const id =
    this.storage.getCreatorAuthUserId() ?? this.storage.getAuthUserId();

    return this.http.get<UsersDTO>(`${this.AUTH}/${id}`,
  {headers: this.h()});  
}

  getAllUsers(): Observable<UsersDTO[]> {
    return this.http.get<UsersDTO[]>(`${this.AUTH}/all`, { headers: this.h() });
  }

  // ── Questions ─────────────────────────────────────────────

  // POST /api/questions
  addQuestion(dto: CreateQuestionDTO): Observable<QuestionDTO> {
    return this.http.post<QuestionDTO>(this.QUESTION, dto, { headers: this.jh() });
  }

  // GET /api/questions
  getAllQuestions(): Observable<QuestionDTO[]> {
    return this.http.get<QuestionDTO[]>(this.QUESTION, { headers: this.h() });
  }

  // GET /api/questions/category/{category}
  getQuestionsByCategory(category: string): Observable<QuestionResponseDTO[]> {
    return this.http.get<QuestionResponseDTO[]>(
      `${this.QUESTION}/internal/category/${category}`, { headers: this.h() }
    );
  }


  // Derived — count questions by category
  getQuestionCountByCategory(category: string): Observable<number> {
    return this.getQuestionsByCategory(category).pipe(map(q => q.length));
  }

  // Derived — count questions by category + difficulty
  getQuestionCountByCategoryAndDifficulty(cat: string, diff: string): Observable<number> {
    return this.getQuestionsByCategory(cat).pipe(
      map(qs => qs.filter(q =>
        q.difficultyLevel.toUpperCase() === diff.toUpperCase()
      ).length)
    );
  }

  // Derived — unique category list
  // getAllCategories(): Observable<string[]> {
  //   return this.getAllQuestions().pipe(
  //     map(qs => [...new Set(qs.map(q => q.category))])
  //   );
  // }

  // DELETE /api/questions/{id}
  deleteQuestion(id: number): Observable<QuestionDTO> {
    return this.http.delete<QuestionDTO>(`${this.QUESTION}/${id}`, { headers: this.h() });
  }

  getCategories(): Observable<string[]> {
  return this.http.get<string[]>(`${this.QUESTION}/categories`,{ headers: this.h() });
}

getQuestionsByCreator(): Observable<QuestionDTO[]> {

    const id = this.storage.getCreatorAuthUserId();

    if(id == null){
        throw new Error("User not logged in.");
    }

    return this.http.get<QuestionDTO[]>(`${this.QUESTION}/creator/${id}`,{headers:this.h()});
}

evaluateResponses(dto : ResponseDTO[]) : Observable<ResponseEvaluationDTO[]>{
      return this.http.post<ResponseEvaluationDTO[]>(`${this.QUESTION}/evaluate`,dto,{headers:this.jh()});
  }
  // ── Quiz CRUD ─────────────────────────────────────────────

  // POST /api/quiz
  createQuiz(dto: CreateQuizDTO): Observable<QuizDTO> {
    return this.http.post<QuizDTO>(this.QUIZ, dto, { headers: this.jh() });
  }

  // GET /api/quiz/{id}
  getQuizById(id: number): Observable<QuizDTO> {
    return this.http.get<QuizDTO>(`${this.QUIZ}/${id}`, { headers: this.h() });
  }

  // GET /api/quiz/title/{title}
  getQuizByQuizTitle(title: string): Observable<QuizDTO> {
    return this.http.get<QuizDTO>(
      `${this.QUIZ}/title/${encodeURIComponent(title)}`, { headers: this.h() }
    );
  }

  // GET /api/quiz/creator/{userId}
  getAllQuizzesByCreator(): Observable<QuizDTO[]> {
    const id = this.storage.getCreatorAuthUserId();

    if (id == null) {
        throw new Error("User not logged in.");
    }

    return this.http.get<QuizDTO[]>(`${this.QUIZ}/creator/${id}`,
        {headers: this.h()});
  }

  // Alias used by "All Quizzes" view — uses same creator endpoint for now
 getAllQuiz(): Observable<QuizDTO[]> {
    return this.http.get<QuizDTO[]>(`${this.QUIZ}/all`,
        {headers: this.h()}
    );
}

  // GET /api/quiz/participant/{userId}  → string[] of titles
  getAllTakenQuizTitles(): Observable<string[]> {
    const userId = this.storage.getCreatorAuthUserId() ?? this.storage.getAuthUserId();

    if (userId == null) {
        throw new Error("User not logged in.");
    }

    return this.http.get<string[]>(`${this.QUIZ}/participant/${userId}`,
        {headers: this.h()}
    );
  }

  // Derived title list from creator quizzes
  getAllQuizTitles(): Observable<string[]> {
    return this.getAllQuizzesByCreator().pipe(map(qs => qs.map(q => q.title)));
  }

  // GET /api/quiz/{quizId}/questions  → string[] of question titles
  getQuestionTitlesOfQuiz(quizId: number): Observable<string[]> {
    return this.http.get<string[]>(`${this.QUIZ}/${quizId}/questions`, { headers: this.h() });
  }

  // DELETE /api/quiz/{id}
  deleteQuiz(id: number): Observable<QuizDTO> {
    return this.http.delete<QuizDTO>(`${this.QUIZ}/${id}`, { headers: this.h() });
  }

  // Duplicate
  // getAvailableQuizzes(): Observable<QuizDTO[]> {
  //   return this.http.get<QuizDTO[]>(`${this.QUIZ}/all`,
  //       {headers: this.h()});
  // }

  

  // ── Start & Submit ────────────────────────────────────────

  // POST /api/quiz/{quizId}/start?userId={userId}
startQuiz(quizId: number): Observable<QuestionResponseDTO[]> {

    const userId = this.storage.getCreatorAuthUserId() ?? this.storage.getAuthUserId();
    if (userId == null) {
        throw new Error("User not logged in.");
    }

    return this.http.post<QuestionResponseDTO[]>(`${this.QUIZ}/${quizId}/start`, null,{
            params: {userId: userId.toString()},
            headers: this.h()
        });
}

  submitQuiz(request: QuizSubmitRequest): Observable<ResultDTO> {

    request.userId = this.storage.getCreatorAuthUserId() ?? this.storage.getAuthUserId() ?? 0;
    return this.http.post<ResultDTO>(`${this.QUIZ}/submit`, request,
        {headers: this.jh()} );
}

// getQuizStats(quizId: number): Observable<QuizStatsDTO> {
//   return this.http.get<QuizStatsDTO>(
//     `${this.QUIZ}/${quizId}/stats`,
//     { headers: this.h() }
//   );
// }

  // ── Results ───────────────────────────────────────────────

  // GET /api/quiz/results/user/{userId}
  getResultsByUser(): Observable<QuizResultDTO[]> {
  const userId = this.storage.getCreatorAuthUserId() ?? this.storage.getAuthUserId();
if (userId == null) {
    throw new Error("User not logged in.");
}

return this.http.get<QuizResultDTO[]>(`${this.QUIZ}/results/user/${userId}`,
    {headers: this.h()});
  }

  // GET /api/quiz/{quizId}/results
  getResultsByQuiz(quizId: number): Observable<QuizResultDTO[]> {
    return this.http.get<QuizResultDTO[]>(`${this.QUIZ}/${quizId}/results`, { headers: this.h() });
  }

  // GET /api/quiz/{quizId}/stats
  getQuizStats(quizId: number): Observable<QuizStatsDTO> {
    return this.http.get<QuizStatsDTO>(`${this.QUIZ}/${quizId}/stats`, { headers: this.h() }
    );
  }

// ── Notification documents ────────────────────────────────

getQuizLeaderboard(quizId : number) : Observable<QuizLeaderboardDTO> {
  return this.http.get<QuizLeaderboardDTO>(`${this.LEADERSHIP}/quiz/${quizId}`, { headers: this.h() });
}

getTopForQuiz(quizId : number , limit : number ) : Observable<LeaderboardEntryDTO[]> {
  return this.http.get<LeaderboardEntryDTO[]>(`${this.LEADERSHIP}/quiz/${quizId}/top`, { 
    params : {limit : limit},
    headers: this.h() });
}

getGlobalLeaderboard(limit : number) : Observable<GlobalLeaderboardDTO> {
  return this.http.get<GlobalLeaderboardDTO>(`${this.LEADERSHIP}/global`, { 
    params : {limit : limit},
    headers: this.h() });
}

getCategoryLeaderboard(category : string) : Observable<CategoryLeaderboardDTO> {
return this.http.get<CategoryLeaderboardDTO>(`${this.LEADERSHIP}/category/${category}`, { headers: this.h() });
}

getParticipantRanking(participantId : number , quizId : number) : Observable<ParticipantRankDTO> {
  return this.http.get<ParticipantRankDTO>(`${this.LEADERSHIP}/participant/${participantId}`, { 
    params : {quizId : quizId},
    headers: this.h() 
  });
}

  // ── Notification documents ────────────────────────────────

  // GET /api/notify/document/participant/{userId}/report
  getParticipantReportDocument(userId: number): Observable<string> {
    return this.http.get(
      `${this.NOTIFY}/document/participant/${userId}/report`,
      { headers: this.h(), responseType: 'text' }
    );
  }

singleResultDocument(quizId: number, userId: number): Observable<string> {
    return this.http.get(
      `${this.NOTIFY}/document/user/${userId}/quiz/${quizId}/result`,
      { headers: this.h(), responseType: 'text' }
    );
  }
  
  // GET /api/notify/document/quiz/{quizId}/results
  getQuizResultDocument(quizId: number): Observable<string> {
    return this.http.get(
      `${this.NOTIFY}/document/quiz/${quizId}/results`,
      { headers: this.h(), responseType: 'text' }
    );
  }

  // POST /api/notify/email/report/{userId}
  sendReportCardEmail(userId: number): Observable<string> {
    return this.http.post(
      `${this.NOTIFY}/email/report/${userId}`, null,
      { headers: this.h(), responseType: 'text' }
    );
  }

  // ======================================================
// REPORT CENTER
// ======================================================

// GET /api/quiz/{quizId}/stats
// getQuizStats(quizId: number): Observable<QuizStatsDTO> {
//   return this.http.get<QuizStatsDTO>(
//     `${this.QUIZ}/${quizId}/stats`,
//     { headers: this.h() }
//   );
// }

// GET /api/quiz/{quizId}/results
getQuizResults(quizId: number): Observable<QuizResultDTO[]> {
  return this.http.get<QuizResultDTO[]>(
    `${this.QUIZ}/${quizId}/results`,
    { headers: this.h() }
  );
}

// GET /api/notify/document/quiz/{quizId}/results
// getQuizResultDocument(quizId: number): Observable<string> {
//   return this.http.get(
//     `${this.NOTIFY}/document/quiz/${quizId}/results`,
//     {
//       headers: this.h(),
//       responseType: 'text'
//     }
//   );
// }

// POST /api/notify/email/result/{userId}/quiz/{quizId}
sendQuizResultEmail(
  userId: number,
  quizId: number
): Observable<string> {

  return this.http.post(
    `${this.NOTIFY}/email/result/${userId}/quiz/${quizId}`,
    null,
    {
      headers: this.h(),
      responseType: 'text'
    }
  );
}

  // POST Generate Question Using AI
   generateAiQuestions(req: AiQuestionGenerateRequest): Observable<AiGeneratedQuestionDTO[]> {
    return this.http.post<AiGeneratedQuestionDTO[]>(
      `${this.QUESTION}/ai/generate`,req,{ headers: this.jh() });
  }
}