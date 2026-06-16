package com.example.quiz_service.controller;

import com.example.quiz_service.dto.applicationDTO.*;
import com.example.quiz_service.dto.notificationDTO.QuizResultDTO;
import com.example.quiz_service.dto.notificationDTO.QuizStatsDTO;
import com.example.quiz_service.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
@CrossOrigin("*")
public class QuizController {

    private final QuizService quizService;

    // 🔒 ADMIN / CURATOR
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','CURATOR')")
    public ResponseEntity<QuizDTO> createQuiz(@RequestBody CreateQuizDto dto) {
        return ResponseEntity.ok(quizService.createQuiz(dto));
    }

    // 🔒 AUTHENTICATED
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuizDTO> getQuiz(@PathVariable Long id) {
        return ResponseEntity.ok(quizService.getQuiz(id));
    }

    // 🔒 AUTHENTICATED
    @GetMapping("/title/{title}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuizDTO> getQuizByTitle(@PathVariable String title) {
        return ResponseEntity.ok(quizService.getQuizByTitle(title));
    }

    // 🔒 ADMIN / CURATOR
    @GetMapping("/creator/{userId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','CURATOR')")
    public ResponseEntity<List<QuizDTO>> getByCreator(@PathVariable Long userId) {
        return ResponseEntity.ok(quizService.getQuizzesByCreator(userId));
    }

    // 🔒 PARTICIPANT
    @GetMapping("/participant/{userId}")
    @PreAuthorize("hasAuthority('PARTICIPANT')")
    public ResponseEntity<List<String>> getParticipantQuizzes(@PathVariable Long userId) {
        return ResponseEntity.ok(quizService.getQuizTitlesByParticipant(userId));
    }

    // 🔒 AUTHENTICATED
    @GetMapping("/{quizId}/questions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> getQuestionTitles(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.getQuestionTitlesOfQuiz(quizId));
    }

    // 🔒 AUTHENTICATED
    @GetMapping("/category/{category}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<QuestionResponseDTO>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(quizService.getQuestionsByCategory(category));
    }

    // 🔒 PARTICIPANT
    @PostMapping("/{quizId}/start")
    @PreAuthorize("hasAuthority('PARTICIPANT')")
    public ResponseEntity<List<QuestionResponseDTO>> startQuiz(
            @PathVariable Long quizId,
            @RequestParam Long userId) {

        return ResponseEntity.ok(quizService.startQuiz(quizId, userId));
    }

    // 🔒 PARTICIPANT
    @PostMapping("/submit")
    @PreAuthorize("hasAuthority('PARTICIPANT')")
    public ResponseEntity<ResultDTO> submitQuiz(@RequestBody QuizTakenRequestDTO request) {
        return ResponseEntity.ok(quizService.submitQuiz(request));
    }
// ===== NOTIFICATION SERVICE ENDPOINTS
    //  All results for a specific quiz
    @GetMapping("/{quizId}/results")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<QuizResultDTO>> getResultsByQuiz(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.getResultsByQuiz(quizId));
    }

    // All results for a participant
    @GetMapping("/results/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<QuizResultDTO>> getResultsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(quizService.getResultsByUser(userId));
    }

    // Aggregated stats for a quiz
    @GetMapping("/{quizId}/stats")
    @PreAuthorize("hasAnyAuthority('ADMIN','CURATOR')")
    public ResponseEntity<QuizStatsDTO> getQuizStats(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.getQuizStats(quizId));
    }

    // DELETE QUIZ
    @DeleteMapping("/{id}")
    @PostAuthorize("hasAnyAuthority('ADMIN','CURATOR')")
    public ResponseEntity<QuizDTO> deleteQuiz(@PathVariable Long id) {
        return ResponseEntity.ok(quizService.deleteQuiz(id));
    }
}