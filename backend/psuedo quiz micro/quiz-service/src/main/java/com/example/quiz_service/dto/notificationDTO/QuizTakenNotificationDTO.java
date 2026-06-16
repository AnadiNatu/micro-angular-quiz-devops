package com.example.quiz_service.dto.notificationDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuizTakenNotificationDTO {
    private Long quizId;
    private String quizTitle;
    private String category;
    private String difficultyLevel;

    private Long participantId;
    private String participantUsername;
    private String participantEmail;

    private Long curatorId;
    private String curatorUsername;
    private String curatorEmail;
}
