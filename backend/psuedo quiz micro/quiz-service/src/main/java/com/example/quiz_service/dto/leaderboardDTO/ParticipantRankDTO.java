package com.example.quiz_service.dto.leaderboardDTO;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantRankDTO {

    private Long participantId;
    private String participantUsername;

    private Integer quizRank;
    private Long rankedQuizId;
    private String rankedQuizTitle;

    private int totalQuizzesTaken;
    private double averagePercentage;
    private double highestPercentage;
    private int totalCorrectAnswers;
    private int totalQuestions;

    private List<LeaderboardEntryDTO> quizHistory;
}
