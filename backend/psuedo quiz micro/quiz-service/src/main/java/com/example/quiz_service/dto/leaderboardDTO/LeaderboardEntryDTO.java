package com.example.quiz_service.dto.leaderboardDTO;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardEntryDTO {

    private int rank;
    private Long participantId;
    private String participantUsername;
    private String participantEmail;

    private Long quizId;
    private String quizTitle;
    private String category;
    private String difficultyLevel;

    private int totalQuestions;
    private int correctAnswers;
    private int incorrectAnswers;
    private double percentage;

    private LocalDateTime takenAt;
}