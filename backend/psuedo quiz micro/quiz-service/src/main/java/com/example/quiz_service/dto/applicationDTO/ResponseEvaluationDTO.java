package com.example.quiz_service.dto.applicationDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseEvaluationDTO {
    private Long questionId;
    private String correctAnswer;
    private String participantAnswer;
}
