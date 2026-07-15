package com.example.quiz_service.controller;

import com.example.quiz_service.dto.leaderboardDTO.*;
import com.example.quiz_service.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
@Slf4j
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    // ===================== QUIZ LEADERBOARD =====================

    // Done
    // Full ranked leaderboard for a single quiz — authenticated users
    @GetMapping("/quiz/{quizId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuizLeaderboardDTO> getQuizLeaderboard(@PathVariable Long quizId) {
        log.info("[LEADERBOARD] Quiz leaderboard request: quizId={}", quizId);
        return ResponseEntity.ok(leaderboardService.getQuizLeaderboard(quizId));
    }

    // Done
    // Top N podium entries for a quiz — useful for trophy/medal display
    @GetMapping("/quiz/{quizId}/top")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LeaderboardEntryDTO>> getTopForQuiz(
            @PathVariable Long quizId,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("[LEADERBOARD] Top-N quiz request: quizId={}, limit={}", quizId, limit);
        return ResponseEntity.ok(leaderboardService.getTopNForQuiz(quizId, limit));
    }

    // ===================== GLOBAL LEADERBOARD =====================

    // Done
    // Global leaderboard aggregated across all quizzes — authenticated users
    @GetMapping("/global")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GlobalLeaderboardDTO> getGlobalLeaderboard(
            @RequestParam(defaultValue = "50") int limit) {
        log.info("[LEADERBOARD] Global leaderboard request: limit={}", limit);
        return ResponseEntity.ok(leaderboardService.getGlobalLeaderboard(limit));
    }

    // ===================== CATEGORY LEADERBOARD =====================

    // Leaderboard filtered by quiz category — authenticated users
    @GetMapping("/category/{category}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CategoryLeaderboardDTO> getCategoryLeaderboard(
            @PathVariable String category) {
        log.info("[LEADERBOARD] Category leaderboard request: category={}", category);
        return ResponseEntity.ok(leaderboardService.getCategoryLeaderboard(category));
    }

    // ===================== PARTICIPANT RANKING =====================

    // Full ranking profile for a participant — their stats + optional quiz-specific rank
    @GetMapping("/participant/{participantId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ParticipantRankDTO> getParticipantRanking(
            @PathVariable Long participantId,
            @RequestParam(required = false) Long quizId) {
        log.info("[LEADERBOARD] Participant ranking request: participantId={}, quizId={}", participantId, quizId);
        return ResponseEntity.ok(leaderboardService.getParticipantRanking(participantId, quizId));
    }
}