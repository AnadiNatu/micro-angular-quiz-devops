package com.example.question_service.dto.applicationDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateQuestionDto {

    private String questionTitle;
    private String category;
    private String difficultyLevel;

    private String rightAnswer;

    private String option1;
    private String option2;
    private String option3;
    private String option4;

    private Long creatorAuthServiceId;
    private String creatorUsername;
    private String creatorRole;
}