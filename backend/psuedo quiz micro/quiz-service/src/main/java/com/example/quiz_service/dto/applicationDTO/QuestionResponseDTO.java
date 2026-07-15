package com.example.quiz_service.dto.applicationDTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionResponseDTO {
    private Long id;
    private String questionTitle;

    private String option1;
    private String option2;
    private String option3;
    private String option4;

    private String rightAnswer;
    private String difficultyLevel;
    private String category;
}
