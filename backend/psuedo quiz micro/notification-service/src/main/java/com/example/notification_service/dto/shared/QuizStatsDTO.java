package com.example.notification_service.dto.shared;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuizStatsDTO {

    private Long quizId;
    private String quizTitle;
    private String category;
    private String difficultyLevel;
    private String creatorUsername;

    private int totalParticipants;
    private int totalQuestions;

    private double averageScore;
    private double averagePercentage;
    private double highestPercentage;
    private double lowestPercentage;

    private long passCount;   // >= 60%
    private long failCount;   // <  60%

    private List<QuizResultDTO> participantResults;
}