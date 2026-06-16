package com.example.question_service.dto.applicationDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseEvaluationDto {
    private String questionTitle;
    private String correctAnswer;
    private String selectedAnswer;
//    private String participantAnswer;
}
