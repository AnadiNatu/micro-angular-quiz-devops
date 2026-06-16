package com.example.quiz_service.dto.applicationDTO;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreatedQuizDTO {
    private String title;
    private String category;
    private String difficultyLevel;

    private int numberOfQuestions;

    private Long createdByUserId;
}
