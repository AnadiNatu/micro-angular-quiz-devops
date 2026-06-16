package com.example.question_service.dto.applicationDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionDto {

    private Long id;

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