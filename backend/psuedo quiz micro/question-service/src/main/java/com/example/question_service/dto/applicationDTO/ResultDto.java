package com.example.question_service.dto.applicationDTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultDto {
//  Summarized info about the quiztakers quiz unfo
    private Long quizId;
    private String quizTitle;
    private Long userId;
    private String userName;
    private int totalQuestions;
    private int correctAnswer;
    private int incorrectAnswer;
    private double percentage;

}
