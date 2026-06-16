package com.example.quiz_service.dto.applicationDTO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResultDTO {
    private Long quizId;
    private String quizTitle;

    private Long userId;
    private String username;

    private int totalQuestions;
    private int correctAnswers;
    private int incorrectAnswers;

    private double percentage;

}
