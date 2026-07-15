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

//@CrossOrigin("*")
@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    // Done
    // 🔒 ADMIN / CURATOR
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','CURATOR')")
    public ResponseEntity<QuizDTO> createQuiz(@RequestBody CreateQuizDto dto) {
        return ResponseEntity.ok(quizService.createQuiz(dto));
    }

    // Done
    // 🔒 AUTHENTICATED
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuizDTO> getQuiz(@PathVariable Long id) {
        return ResponseEntity.ok(quizService.getQuiz(id));
    }

    // Done
    // 🔒 AUTHENTICATED
    @GetMapping("/title/{title}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuizDTO> getQuizByTitle(@PathVariable String title) {
        return ResponseEntity.ok(quizService.getQuizByTitle(title));
    }

    // Done
    // 🔒 ADMIN / CURATOR
    @GetMapping("/creator/{creatorUserId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','CURATOR' , 'PARTICIPANT')")
    public ResponseEntity<List<QuizDTO>> getByCreator(@PathVariable("creatorUserId") Long creatorUserId) {
        return ResponseEntity.ok(quizService.getQuizzesByCreator(creatorUserId));
    }

    // Done
    // 🔒 PARTICIPANT
    @GetMapping("/participant/{userId}")
    @PreAuthorize("hasAuthority('PARTICIPANT')")
    public ResponseEntity<List<String>> getParticipantQuizzes(@PathVariable Long userId) {
        return ResponseEntity.ok(quizService.getQuizTitlesByParticipant(userId));
    }

    // Done
    // 🔒 AUTHENTICATED
    @GetMapping("/{quizId}/questions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> getQuestionTitles(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.getQuestionTitlesOfQuiz(quizId));
    }

    // 🔒 AUTHENTICATED
    @GetMapping("/questions/category/{category}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<QuestionResponseDTO>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(quizService.getQuestionsByCategory(category));
    }

    // Done
    // 🔒 PARTICIPANT
    @PostMapping("/{quizId}/start")
    @PreAuthorize("hasAnyAuthority('PARTICIPANT' , 'CURATOR' , 'ADMIN')")
    public ResponseEntity<List<QuestionResponseDTO>> startQuiz(
            @PathVariable Long quizId,
            @RequestParam Long userId) {

        return ResponseEntity.ok(quizService.startQuiz(quizId, userId));
    }

    // Done
    // 🔒 PARTICIPANT
    @PostMapping("/submit")
    @PreAuthorize("hasAnyAuthority('PARTICIPANT' , 'ADMIN' , 'CURATOR')")
    public ResponseEntity<ResultDTO> submitQuiz(@RequestBody QuizTakenRequestDTO request) {
        return ResponseEntity.ok(quizService.submitQuiz(request));
    }
// ===== NOTIFICATION SERVICE ENDPOINTS
    // Done
    //  All results for a specific quiz
    @GetMapping("/{quizId}/results")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<QuizResultDTO>> getResultsByQuiz(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.getResultsByQuiz(quizId));
    }

    // Done
    // All results for a participant
    @GetMapping("/results/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<QuizResultDTO>> getResultsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(quizService.getResultsByUser(userId));
    }

    // Done
    // Aggregated stats for a quiz
    @GetMapping("/{quizId}/stats")
    @PreAuthorize("hasAnyAuthority('ADMIN','CURATOR')")
    public ResponseEntity<QuizStatsDTO> getQuizStats(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.getQuizStats(quizId));
    }

    // Done
    // DELETE QUIZ
    @DeleteMapping("/{id}")
    @PostAuthorize("hasAnyAuthority('ADMIN','CURATOR')")
    public ResponseEntity<QuizDTO> deleteQuiz(@PathVariable Long id) {
        return ResponseEntity.ok(quizService.deleteQuiz(id));
    }

    // Done
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<QuizDTO>> getAllQuizzes() {
        return ResponseEntity.ok(quizService.getAllQuizzes());
    }
}