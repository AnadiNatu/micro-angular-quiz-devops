package com.example.quiz_service.dto.leaderboardDTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalRankEntryDTO {

    private int rank;
    private Long participantId;          // authServiceId
    private String participantUsername;
    private String participantEmail;

    private int totalQuizzesTaken;
    private int totalCorrectAnswers;
    private int totalQuestions;
    private double averagePercentage;
    private double highestPercentage;

}
