package com.example.quiz_service.dto.leaderboardDTO;


import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizLeaderboardDTO {

    private Long quizId;
    private String quizTitle;
    private String category;
    private String difficultyLevel;
    private String creatorUsername;

    private int totalParticipants;
    private double averagePercentage;
    private double highestPercentage;
    private double lowestPercentage;

    private List<LeaderboardEntryDTO> rankings;
}