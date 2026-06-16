package com.example.quiz_service.dto.notificationDTO;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuizResultDTO {
    private Long id;
    private Long quizId;
    private String quizTitle;
    private String category;
    private String difficultyLevel;

    private Long participantId;
    private String participantUsername;
    private String participantEmail;

    private Long curatorId;
    private String curatorUsername;

    private int totalQuestions;
    private int correctAnswers;
    private int incorrectAnswers;
    private double percentage;

    private LocalDateTime takenAt;
}