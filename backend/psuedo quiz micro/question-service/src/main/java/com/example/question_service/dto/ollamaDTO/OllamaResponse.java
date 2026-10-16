package com.example.question_service.dto.ollamaDTO;

import lombok.Data;

import java.util.List;

@Data
public class OllamaResponse {
    private List<OllamaChoice> choices;
}