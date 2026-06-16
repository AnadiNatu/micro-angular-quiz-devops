package com.example.question_service.dto.aiDTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiQuestionGenerateRequest {
    private String topic;
    private String category;
    private String difficulty;
    private int count;
}