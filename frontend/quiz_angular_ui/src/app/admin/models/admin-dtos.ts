// ─── Question ────────────────────────────────────────────

export interface CreateQuestionDTO {
  questionTitle:   string;
  category:        string;
  difficultyLevel: string;
  rightAnswer:     string;
  option1: string;
  option2: string;
  option3: string;
  option4: string;

    creatorAuthServiceId?: number;
  creatorUsername?:      string;
  creatorRole?:          string;
}

export interface QuestionDTO {
  id:              number;
  questionTitle:   string;
  category:        string;
  difficultyLevel: string;
  rightAnswer:     string;
  option1: string;
  option2: string;
  option3: string;
  option4: string;
}

// Returned by POST /api/quiz/{id}/start  and  POST /api/questions/fetch
export interface QuestionResponseDTO {
  id:              number;
  questionTitle:   string;
  option1: string;
  option2: string;
  option3: string;
  option4: string;
  rightAnswer:     string;
}

// ─── Quiz ─────────────────────────────────────────────────

// POST /api/quiz  body
export interface CreateQuizDTO {
  title:             string;
  category:          string;
  difficultyLevel:   string;
  numberOfQuestions: number;
  createdByUserId:   number;
}

// GET /api/quiz/{id}  response
export interface QuizDTO {
  id:                   number;
  title:                string;
  category:             string;
  difficultyLevel:      string;
  createdByUserId:      number;
  creatorUsername:      string;
  questionIds:          number[];
  participantIds:       number[];
  participantUsernames: string[];
}

// ─── Submit ────────────────────────────────────────────────

// One answer entry in submit body
export interface ResponseDTO {
  questionId:     number;
  selectedAnswer: string | null;
}

// POST /api/quiz/submit  body
export interface QuizSubmitRequest {
  quizId:    number;
  userId:    number;
  responses: ResponseDTO[];
}

// POST /api/quiz/submit  response
export interface ResultDTO {
  quizId:           number;
  quizTitle:        string;
  userId:           number;
  username:         string;
  totalQuestions:   number;
  correctAnswers:   number;
  incorrectAnswers: number;
  percentage:       number;
}

// ─── Results / Stats ──────────────────────────────────────

// GET /api/quiz/{quizId}/results  and  GET /api/quiz/results/user/{userId}
export interface QuizResultDTO {
  id:                  number;
  quizId:              number;
  quizTitle:           string;
  category:            string;
  difficultyLevel:     string;
  participantId:       number;
  participantUsername: string;
  participantEmail:    string;
  curatorId:           number;
  curatorUsername:     string;
  totalQuestions:      number;
  correctAnswers:      number;
  incorrectAnswers:    number;
  percentage:          number;
  takenAt:             string;
}

// GET /api/quiz/{quizId}/stats
export interface QuizStatsDTO {
  quizId:             number;
  quizTitle:          string;
  category:           string;
  difficultyLevel:    string;
  creatorUsername:    string;
  totalParticipants:  number;
  totalQuestions:     number;
  averageScore:       number;
  averagePercentage:  number;
  highestPercentage:  number;
  lowestPercentage:   number;
  passCount:          number;
  failCount:          number;
  participantResults: QuizResultDTO[];
}

// ─── Legacy stubs (keep for components not yet fully updated) ─

/** @deprecated — kept only so un-migrated components compile */
export interface CreatedQuizDTO {
  title:           string;
  category:        string;
  difficultyLevel: string;
  creatorName:     string;
  questionList:    QuestionDTO[];
}

/** @deprecated */
export interface CreatorUserDTO {
  creatorName:       string;
  userRoles:         string;
  questionTitleList: string[];
}

/** @deprecated  use ResponseDTO */
export interface QuizTakenReponse {
  quizTitle:    string;
  userName:     string;
  responseList: { questionTitle: string; selectedAnswer: string | null }[];
}

/** @deprecated */
export interface ResponseEvaluationDTO {
  questionTitle:     string;
  correctAnswer:     string;
  participantAnswer: string;
}

/** @deprecated use QuestionResponseDTO */
export interface QuestionWrapper {
  questionTitle: string;
  option1: string;
  option2: string;
  option3: string;
  option4: string;
}

// ─── AI DTO ──────────────────────────────────────

/** Body sent to POST /api/questions/ai/generate */
export interface AiQuestionGenerateRequest {
  topic:      string;   // e.g. "Binary Trees"
  category:   string;   // must match an existing category
  difficulty: string;   // EASY | MEDIUM | HARD
  count:      number;   // 1–20
}
 
/** Shape of each object returned in the preview list */
export interface AiGeneratedQuestionDTO {
  question:      string;
  optionA:       string;
  optionB:       string;
  optionC:       string;
  optionD:       string;
  correctAnswer: string;  // full text of the correct option
}