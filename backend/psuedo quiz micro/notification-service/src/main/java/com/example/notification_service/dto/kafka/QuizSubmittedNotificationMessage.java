package com.example.notification_service.dto.kafka;

import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmittedNotificationMessage implements Serializable {

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
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer incorrectAnswers;
    private Double percentage;

}