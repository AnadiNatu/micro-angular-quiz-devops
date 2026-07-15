package com.example.quiz_service.service;

import com.example.quiz_service.dto.applicationDTO.*;
import com.example.quiz_service.dto.authDTO.UserStatSyncDTO;
import com.example.quiz_service.dto.notificationDTO.QuizResultDTO;
import com.example.quiz_service.dto.notificationDTO.QuizStatsDTO;
import com.example.quiz_service.dto.notificationDTO.QuizSubmittedNotificationDTO;
import com.example.quiz_service.dto.notificationDTO.QuizTakenNotificationDTO;
import com.example.quiz_service.entity.Quiz;
import com.example.quiz_service.entity.QuizResult;
import com.example.quiz_service.feign.NotificationFeignClient;
import com.example.quiz_service.feign.QuestionClient;
import com.example.quiz_service.feign.UserClient;
import com.example.quiz_service.mapper.QuizMapper;
import com.example.quiz_service.repository.QuizRepository;
import com.example.quiz_service.repository.QuizResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuestionClient questionClient;
    private final UserClient userClient;
    private final QuizResultRepository quizResultRepository;
    private final NotificationFeignClient notificationClient;

    // ================= CREATE QUIZ =================
    public QuizDTO createQuiz(CreateQuizDto dto) {

        List<Long> questionIds = questionClient.generateQuestions(
                dto.getCategory(),
                dto.getNumberOfQuestions()
        );

        if(questionIds.size()!=dto.getNumberOfQuestions()){
            throw new RuntimeException("Only " +questionIds.size()+" questions available.");
        }

        UserResponseDTO creator = userClient.getUserByAuthServiceId(dto.getCreatedByUserId());
        Quiz quiz = QuizMapper.toEntity(dto, questionIds);

//        Store creator name
        quiz.setCreatorUsername(creator.getUsername());

        Quiz saved = quizRepository.save(quiz);

        try {
            userClient.syncStat(UserStatSyncDTO.builder()
                    .authServiceId(dto.getCreatedByUserId())
                    .statType("QUIZ_CREATED")
                    .build());
        } catch (Exception e) {
            log.warn("[QUIZ] Failed to sync QUIZ_CREATED stat: {}", e.getMessage());
        }

        return QuizMapper.toDTO(saved, creator, null);
    }

    // ================= GET QUIZ BY ID =================
    public QuizDTO getQuiz(Long quizId) {

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        UserResponseDTO creator = safeGetUser(quiz.getCreatedByUserId(), quiz.getCreatorUsername());

        List<UserResponseDTO> participants = quiz.getParticipantIds() != null
                ? quiz.getParticipantIds().stream()
                  .map(pid -> safeGetUser(pid, null))
                  .toList()
                : null;

        return QuizMapper.toDTO(quiz, creator, participants);
    }

    // ================= GET QUIZ BY TITLE (OLD FEATURE) =================
    public QuizDTO getQuizByTitle(String title) {

        Quiz quiz = quizRepository.findByTitleIgnoreCase(title)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        return getQuiz(quiz.getId());
    }

    // ================= GET ALL QUIZ BY CREATOR =================
    public List<QuizDTO> getQuizzesByCreator(Long userId) {

        List<Quiz> quizzes = quizRepository.findByCreatedByUserId(userId);
        if (quizzes.isEmpty()) return List.of();

        return quizzes.stream()
                .map(q -> {
                    UserResponseDTO creator = safeGetUser(userId, q.getCreatorUsername());
                    return QuizMapper.toDTO(q, creator, null);
                })
                .toList();
    }

    // ================= GET ALL QUIZ TITLES BY PARTICIPANT =================
    public List<String> getQuizTitlesByParticipant(Long userId) {

        return quizRepository.findByParticipantId(userId)
                .stream()
                .map(Quiz::getTitle)
                .toList();
    }

    // ================= GET QUESTIONS OF QUIZ =================
    public List<String> getQuestionTitlesOfQuiz(Long quizId) {

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        List<QuestionResponseDTO> questions =
                questionClient.getQuestionsByIds(quiz.getQuestionIds());

        return questions.stream()
                .map(QuestionResponseDTO::getQuestionTitle)
                .toList();
    }

    // ================= GET QUESTIONS BY CATEGORY =================
    public List<QuestionResponseDTO> getQuestionsByCategory(String category) {

        // delegate to question-service
        return questionClient.getQuestionsByCategory(category);
    }

    // START QUIZ (PARTICIPATION)
    public List<QuestionResponseDTO> startQuiz(Long quizId, Long userId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean testUser = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("CURATOR"));

        // Guard: participant can only take a quiz once
        if (!testUser &&
                quiz.getParticipantIds() != null &&
                quiz.getParticipantIds().contains(userId)) {

            throw new RuntimeException("User has already taken this quiz");
        }

        // Initialize list if null and add participant
//        if (quiz.getParticipantIds() == null) {
//            quiz.setParticipantIds(new ArrayList<>());
//        }
//        quiz.getParticipantIds().add(userId);
//        quizRepository.save(quiz);
        if (!testUser) {
            if (quiz.getParticipantIds() == null) {
                quiz.setParticipantIds(new ArrayList<>());
            }
            quiz.getParticipantIds().add(userId);
            quizRepository.save(quiz);
        } else {
            log.info("[QUIZ] TEST MODE -> {} is previewing quiz {}", authentication.getName(), quizId);
        }

        // Fetch user details for notification (best-effort, after the save so
        // a Feign failure here does not roll back the participation record)
//        UserResponseDTO participant = userClient.getUserByAuthServiceId(userId);
//        UserResponseDTO curator     = userClient.getUserByAuthServiceId(quiz.getCreatedByUserId());

        UserResponseDTO participant=null;
        UserResponseDTO curator=null;
        try {
            participant = userClient.getUserByAuthServiceId(userId);
            curator = userClient.getUserByAuthServiceId(quiz.getCreatedByUserId());
        }catch (Exception ex){
            log.warn("[QUIZ] Could not fetch user details for notification: {}", ex.getMessage());
        }

//        Increment the Quiz taken counter
        if (!testUser) {
            try {
                userClient.syncStat(UserStatSyncDTO.builder()
                        .authServiceId(userId)
                        .statType("QUIZ_TAKEN")
                        .build());
            } catch (Exception e) {
                log.warn("[QUIZ] Failed to sync QUIZ_TAKEN stat: {}", e.getMessage());
            }
        }

        // Fire quiz-taken notification (best-effort)
        if (participant != null && curator != null) {
            try {
                QuizTakenNotificationDTO notifDto = QuizTakenNotificationDTO.builder()
                        .quizId(quiz.getId())
                        .quizTitle(quiz.getTitle())
                        .category(quiz.getCategory())
                        .difficultyLevel(quiz.getDifficultyLevel())
                        .participantId(userId)
                        .participantUsername(participant.getUsername())
                        .participantEmail(participant.getEmail())
                        .curatorId(quiz.getCreatedByUserId())
                        .curatorUsername(curator.getUsername())
                        .curatorEmail(curator.getEmail())
                        .build();

                if (!testUser) {
                    notificationClient.notifyQuizTaken(notifDto);
                }
                } catch (Exception e) {
                log.warn("⚠️ Failed to send quiz-taken notification: {}", e.getMessage());
            }
        }
        return questionClient.getQuestionsByIds(quiz.getQuestionIds());
    }

    // ================= SUBMIT QUIZ =================
    public ResultDTO submitQuiz(QuizTakenRequestDTO request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean testUser = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("CURATOR"));


        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        List<QuestionResponseDTO> questions =
                questionClient.getQuestionsByIds(quiz.getQuestionIds());

        // ── Score calculation ──
        int correct = 0;
        int answered = request.getResponses().size();

        for (ResponseDTO response : request.getResponses()) {
            QuestionResponseDTO q = questions.stream()
                    .filter(x -> x.getId().equals(response.getQuestionId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Question not found in quiz"));

            if (response.getSelectedAnswer() != null && response.getSelectedAnswer().equalsIgnoreCase(q.getRightAnswer())) {
                correct++;
            }
        }

        int total = quiz.getQuestionIds().size();
        int incorrect = answered - correct;
        int skipped = total - answered;
        double percentage = (total > 0) ? (correct * 100.0) / total : 0;

        UserResponseDTO participant = userClient.getUserByAuthServiceId(request.getUserId());
        UserResponseDTO curator     = userClient.getUserByAuthServiceId(quiz.getCreatedByUserId());

        // ✅ Persist result (upsert-style: skip if already exists)
//        if (!quizResultRepository.existsByQuizIdAndParticipantId(quiz.getId(), request.getUserId())) {

        if (!testUser && !quizResultRepository.existsByQuizIdAndParticipantId(quiz.getId(), request.getUserId())) {
            QuizResult result = QuizResult.builder()
                    .quizId(quiz.getId())
                    .quizTitle(quiz.getTitle())
                    .category(quiz.getCategory())
                    .difficultyLevel(quiz.getDifficultyLevel())
                    .participantId(request.getUserId())
                    .participantUsername(participant.getUsername())
                    .participantEmail(participant.getEmail())
                    .curatorId(quiz.getCreatedByUserId())
                    .curatorUsername(curator.getUsername())
                    .totalQuestions(total)
                    .correctAnswers(correct)
                    .incorrectAnswers(incorrect)
                    .percentage(percentage)
                    .build();

            result = quizResultRepository.save(result);

            log.info("Quiz Result Saved : {}", result.getId());
            log.info("[QUIZ] Result saved — quizId={} userId={} correct={}/{} ({:.1f}%)",
                    quiz.getId(), request.getUserId(), correct, total, percentage);
        }else if (!testUser) {
            log.warn("[QUIZ] Duplicate submit ignored — quizId={} userId={}", quiz.getId(), request.getUserId());
        } else {
            log.info("[QUIZ] TEST MODE -> Result not persisted for {}", authentication.getName());
        }

        // Fire quiz-submitted notification (best-effort)
        if (!testUser) {
            try {
                QuizSubmittedNotificationDTO notifDto = QuizSubmittedNotificationDTO.builder()
                        .quizId(quiz.getId())
                        .quizTitle(quiz.getTitle())
                        .category(quiz.getCategory())
                        .difficultyLevel(quiz.getDifficultyLevel())
                        .participantId(request.getUserId())
                        .participantUsername(participant.getUsername())
                        .participantEmail(participant.getEmail())
                        .curatorId(quiz.getCreatedByUserId())
                        .curatorUsername(curator.getUsername())
                        .curatorEmail(curator.getEmail())
                        .totalQuestions(total)
                        .correctAnswers(correct)
                        .incorrectAnswers(incorrect)
                        .percentage(percentage)
                        .build();

                notificationClient.notifyQuizSubmitted(notifDto);
            } catch (Exception e) {
                log.warn("Failed to send quiz-submitted notification: {}", e.getMessage());
            }
        }

        return ResultDTO.builder()
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .userId(participant.getId())
                .username(participant.getUsername())
                .totalQuestions(total)
                .correctAnswers(correct)
                .incorrectAnswers(incorrect)
                .percentage(percentage)
                .build();
    }

//    RESULT QUERIES

//    ---> ALL PARTICIPANT SINGLE QUIZ RESULT
public List<QuizResultDTO> getResultsByQuiz(Long quizId) {
    return quizResultRepository.findByQuizId(quizId).stream()
            .map(this::toResultDTO).toList();
}

//   ---> SINGLE PARTICIPANT SINGLE QUIZ RESULT
    public List<QuizResultDTO> getResultsByUser(Long userId) {
        return quizResultRepository.findByParticipantId(userId).stream()
                .map(this::toResultDTO).toList();
    }

//    ---->  QUIZ STATS
    public QuizStatsDTO getQuizStats(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        UserResponseDTO creator = userClient.getUserByAuthServiceId(quiz.getCreatedByUserId());
        List<QuizResultDTO> results = getResultsByQuiz(quizId);

        double avgPct = results.stream().mapToDouble(QuizResultDTO::getPercentage).average().orElse(0);
        double high   = results.stream().mapToDouble(QuizResultDTO::getPercentage).max().orElse(0);
        double low    = results.stream().mapToDouble(QuizResultDTO::getPercentage).min().orElse(0);
        long   passed = results.stream().filter(r -> r.getPercentage() >= 60).count();

        return QuizStatsDTO.builder()
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .category(quiz.getCategory())
                .difficultyLevel(quiz.getDifficultyLevel())
                .creatorUsername(creator.getUsername())
                .totalParticipants(results.size())
                .totalQuestions(quiz.getQuestionIds() != null ? quiz.getQuestionIds().size() : 0)
                .averagePercentage(avgPct)
                .highestPercentage(high)
                .lowestPercentage(low)
                .passCount(passed)
                .failCount(results.size() - passed)
                .participantResults(results)
                .build();
    }




    // ================= DELETE QUIZ =================
    public QuizDTO deleteQuiz(Long id) {

        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        UserResponseDTO creator =  null;

        try{
            creator = userClient.getUserByAuthServiceId(quiz.getCreatedByUserId());
        }catch (Exception ex){
            log.warn("[QUIZ] Could not fetch creator during delete — using stored username: {}", ex.getMessage());
            // Fall back to the denormalised username stored on the quiz entity itself
            creator = UserResponseDTO.builder()
                    .id(quiz.getCreatedByUserId())
                    .username(quiz.getCreatorUsername())
                    .build();
        }

        QuizDTO dto = QuizMapper.toDTO(quiz, creator, null);
        quizRepository.delete(quiz);
        log.info("[QUIZ] Quiz deleted — id={} title={}", id, quiz.getTitle());
        return dto; // 🔥 needed for @PostAuthorize
    }

//    =========== HELPERS METHOD==========

    private QuizResultDTO toResultDTO(QuizResult r) {
        return QuizResultDTO.builder()
                .id(r.getId())
                .quizId(r.getQuizId())
                .quizTitle(r.getQuizTitle())
                .category(r.getCategory())
                .difficultyLevel(r.getDifficultyLevel())
                .participantId(r.getParticipantId())
                .participantUsername(r.getParticipantUsername())
                .participantEmail(r.getParticipantEmail())
                .curatorId(r.getCuratorId())
                .curatorUsername(r.getCuratorUsername())
                .totalQuestions(r.getTotalQuestions())
                .correctAnswers(r.getCorrectAnswers())
                .incorrectAnswers(r.getIncorrectAnswers())
                .percentage(r.getPercentage())
                .takenAt(r.getTakenAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<QuizDTO> getAllQuizzes() {

        List<Quiz> quizzes = quizRepository.findAll();

        if (quizzes.isEmpty()) {
            return List.of();
        }

        // Cache creator information to avoid repeated Feign calls
        Map<Long, UserResponseDTO> creatorCache = new HashMap<>();

        return quizzes.stream()
                .map(quiz -> {

                    UserResponseDTO creator = creatorCache.computeIfAbsent(
                            quiz.getCreatedByUserId(),
                            id -> safeGetUser(id, quiz.getCreatorUsername())
                    );

                    return QuizMapper.toDTO(quiz, creator, null);

                })
                .toList();
    }

    private UserResponseDTO safeGetUser(Long authServiceId, String fallbackUsername) {
        try {
            return userClient.getUserByAuthServiceId(authServiceId);
        } catch (Exception ex) {
            log.warn("[QUIZ] Could not fetch user id={} from auth-service, using fallback: {}",
                    authServiceId, ex.getMessage());
            return UserResponseDTO.builder()
                    .id(authServiceId)
                    .username(fallbackUsername != null ? fallbackUsername : "Unknown")
                    .build();
        }
    }
}
