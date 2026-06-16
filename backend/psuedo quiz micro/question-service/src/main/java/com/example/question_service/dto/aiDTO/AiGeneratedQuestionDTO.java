package com.example.question_service.dto.aiDTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiGeneratedQuestionDTO {

    private String question;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer;

}