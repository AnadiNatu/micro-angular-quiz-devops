package com.example.quiz_service.dto.applicationDTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateQuizDto {
    private Long id;
    private String title;
    private String category;
    private String difficultyLevel;

    private int numberOfQuestions;

    private Long createdByUserId; //

}
