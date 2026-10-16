package com.example.question_service.dto.ollamaDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OllamaMessage {
    private String role;
    private String content;
}