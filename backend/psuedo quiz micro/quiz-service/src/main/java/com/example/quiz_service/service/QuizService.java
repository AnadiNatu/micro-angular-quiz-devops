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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

        if (questionIds == null || questionIds.isEmpty()) {
            throw new RuntimeException("No questions available");
        }

        UserResponseDTO creator = userClient.getUserById(dto.getCreatedByUserId());
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

        UserResponseDTO creator = userClient.getUserById(quiz.getCreatedByUserId());

        List<UserResponseDTO> participants = quiz.getParticipantIds() != null
                ? quiz.getParticipantIds().stream()
                .map(userClient::getUserById)
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

        return quizRepository.findByCreatedByUserId(userId)
                .stream()
                .map(q -> {
                    UserResponseDTO creator = userClient.getUserById(userId);
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

        // Guard: participant can only take a quiz once
        if (quiz.getParticipantIds() != null && quiz.getParticipantIds().contains(userId)) {
            throw new RuntimeException("User has already taken this quiz");
        }

        // Initialize list if null and add participant
        if (quiz.getParticipantIds() == null) {
            quiz.setParticipantIds(new ArrayList<>());
        }
        quiz.getParticipantIds().add(userId);
        quizRepository.save(quiz);

        // Enrich: get participant + curator info
        UserResponseDTO participant = userClient.getUserById(userId);
        UserResponseDTO curator     = userClient.getUserById(quiz.getCreatedByUserId());

//        Increment the Quiz taken counter
        try {
            userClient.syncStat(UserStatSyncDTO.builder()
                    .authServiceId(userId)
                    .statType("QUIZ_TAKEN")
                    .build());
        } catch (Exception e) {
            log.warn("[QUIZ] Failed to sync QUIZ_TAKEN stat: {}", e.getMessage());
        }

        // Fire quiz-taken notification (best-effort)
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

            notificationClient.notifyQuizTaken(notifDto);
        } catch (Exception e) {
            log.warn("⚠️ Failed to send quiz-taken notification: {}", e.getMessage());
        }

        return questionClient.getQuestionsByIds(quiz.getQuestionIds());
    }

    // ================= SUBMIT QUIZ =================
    public ResultDTO submitQuiz(QuizTakenRequestDTO request) {
        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        List<QuestionResponseDTO> questions =
                questionClient.getQuestionsByIds(quiz.getQuestionIds());

        // ── Score calculation ──
        int correct = 0;
        for (ResponseDTO response : request.getResponses()) {
            QuestionResponseDTO q = questions.stream()
                    .filter(x -> x.getId().equals(response.getQuestionId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Question not found in quiz"));

            if (response.getSelectedAnswer().equalsIgnoreCase(q.getRightAnswer())) {
                correct++;
            }
        }

        int total = quiz.getQuestionIds().size();
        int incorrect = total - correct;
        double percentage = (total > 0) ? (correct * 100.0) / total : 0;

        UserResponseDTO participant = userClient.getUserById(request.getUserId());
        UserResponseDTO curator     = userClient.getUserById(quiz.getCreatedByUserId());

        // ✅ Persist result (upsert-style: skip if already exists)
        if (!quizResultRepository.existsByQuizIdAndParticipantId(quiz.getId(), request.getUserId())) {
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

            quizResultRepository.save(result);
        }

        // Fire quiz-submitted notification (best-effort)
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

        UserResponseDTO creator = userClient.getUserById(quiz.getCreatedByUserId());
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

        QuizDTO dto = QuizMapper.toDTO(quiz, null, null);

        quizRepository.delete(quiz);

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
}
